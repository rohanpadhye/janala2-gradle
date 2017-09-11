package janala.instrument;

import janala.config.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("unused") // Registered via -javaagent
public class SnoopInstructionTransformer implements ClassFileTransformer {
  private boolean writeInstrumentedClasses = true;
  private String instDir = "instrumented";
  public SnoopInstructionTransformer() {
    writeInstrumentedClasses = Config.instance.writeInstrumentedClasses;
    instDir = "instrumented";
  }
  
  private static String[] banned = {"[", "java/lang", "com/sun", "janala", "org/objectweb/asm", "sun", "jdk", "java/util/function"};
  private static String[]  excludes;
  private static String[]  includes;
  
  public static void premain(String agentArgs, Instrumentation inst) throws ClassNotFoundException {

    preloadClasses();

    Coverage.read(Config.instance.coverage);
    excludes = Config.instance.excludeInst;
    includes = Config.instance.includeInst;
    inst.addTransformer(new SnoopInstructionTransformer(), true);
    if (inst.isRetransformClassesSupported()) {
      for (Class clazz : inst.getAllLoadedClasses()) {
        try {
          String cname = clazz.getName().replace(".","/");
          if (shouldExclude(cname) == false) {
            if (inst.isModifiableClass(clazz)) {
              inst.retransformClasses(clazz);
            } else {
              System.err.println("[JANALA] Could not instrument " + clazz + " :-(");
            }
          }
        } catch (Exception e){
          e.printStackTrace();
        }
      }
    }
  }

  private static void preloadClasses() throws ClassNotFoundException {
    Class.forName("java.util.ArrayDeque");
    Class.forName("java.util.LinkedList");
    Class.forName("java.util.LinkedList$Node");
    Class.forName("java.util.LinkedList$ListItr");
    Class.forName("java.util.TreeMap");
    Class.forName("java.util.TreeMap$Entry");
  }

  /** packages that should be exluded from the instrumentation */
  private static boolean shouldExclude(String cname) {
    for (String e : banned) {
      if (cname.startsWith(e)) {
        return true;
      }
    }
    for (String e : includes) {
      if (cname.startsWith(e)) {
        return false;
      }
    }
    for (String e : excludes) {
      if (cname.startsWith(e)) {
        return true;
      }
    }
    return false;
  }

  static Map<String, byte[]> instrumentedBytes = new TreeMap<>();

  @Override
  synchronized public byte[] transform(ClassLoader loader, String cname, Class<?> classBeingRedefined,
      ProtectionDomain d, byte[] cbuf)
    throws IllegalClassFormatException {

    boolean toInstrument = !shouldExclude(cname);

    if (toInstrument) {
      System.err.print("[JANALA] ");
      if (classBeingRedefined != null) {
        System.err.print("* ");
      }
      System.err.print("Instrumenting: " + cname + "... ");
      GlobalStateForInstrumentation.instance.setCid(Coverage.instance.getCid(cname));

      if (instrumentedBytes.containsKey(cname)) {
        System.err.println(" Found in fast-cache!");
        return instrumentedBytes.get(cname);
      }

      File cachedFile = new File(instDir + "/" + cname + ".class");
      if (cachedFile.exists()) {
        try {
          byte[] instBytes = Files.readAllBytes(cachedFile.toPath());
          System.err.println(" Found in disk-cache!");
          instrumentedBytes.put(cname, instBytes);
          return instBytes;
        } catch (IOException e) {
          System.err.print(" <cache error> ");
        }
      }


      ClassReader cr = new ClassReader(cbuf);
      ClassWriter cw = new SafeClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
      ClassVisitor cv = new SnoopInstructionClassAdapter(cw, cname);

      try {
        cr.accept(cv, 0);
      } catch (Throwable e) {
        e.printStackTrace();
        return null;
      }

      byte[] ret = cw.toByteArray();
      System.err.println("Done!");
      instrumentedBytes.put(cname, ret);

      if (writeInstrumentedClasses) {
        try {
          File file = new File(instDir + "/" + cname + ".class");
          File parent = new File(file.getParent());
          parent.mkdirs();
          FileOutputStream out = new FileOutputStream(file);
          out.write(ret);
          out.close();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
      return ret;
    } else {
      return cbuf;
    }
  }
}
