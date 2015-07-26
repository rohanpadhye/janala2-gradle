package janala.instrument;

import janala.config.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class SnoopInstructionTransformer implements ClassFileTransformer {
  private boolean writeInstrumentedClasses = true;
  private String instDir = "instrumented";
  public SnoopInstructionTransformer() {
    writeInstrumentedClasses = true;
    instDir = "instrumented";
  }
  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new SnoopInstructionTransformer());
  }

  /** packages that should be exluded from the instrumentation */
  private static boolean shouldExclude(String cname) {
    String[] exclude = {"sun", "java", "com/google/monitoring", "janala"};
    for (String e : exclude) {
      if (cname.startsWith(e)) {
        return true;
      }
    }
    return false;
  }

  public byte[] transform(ClassLoader loader, String cname, Class<?> classBeingRedefined,
      ProtectionDomain d, byte[] cbuf)
    throws IllegalClassFormatException {

    if (classBeingRedefined != null) {
      return null;
    }

    boolean toInstrument = !shouldExclude(cname);

    /*
    String[] tmp = Config.instance.excludeList;
    for (int i = 0; i < tmp.length; i++) {
      String s = tmp[i];
      if (cname.startsWith(s)) {
        toInstrument = false;
        break;
      }
    }

    tmp = Config.instance.includeList;
    for (int i = 0; i < tmp.length; i++) {
      String s = tmp[i];
      if (cname.startsWith(s)) {
        toInstrument = true;
        break;
      }
      } */

    if (toInstrument) {
      System.out.println("begin transform " + cname);
      Coverage.read();
      GlobalStateForInstrumentation.instance.setCid(Coverage.instance.getCid(cname));
      ClassReader cr = new ClassReader(cbuf);
      ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
      ClassVisitor cv = new SnoopInstructionClassAdapter(cw);

      try {
        cr.accept(cv, 0);
      } catch (Exception e) {
        e.printStackTrace();
      }

      byte[] ret = cw.toByteArray();
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
    }
    return cbuf;
  }
}