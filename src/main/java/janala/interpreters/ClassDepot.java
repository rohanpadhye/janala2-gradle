package janala.interpreters;

import janala.utils.MyLogger;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassDepot {

  private Map<String, ClassTemplate> templates;

  public final static ClassDepot instance = new ClassDepot();
  private final static Logger logger = MyLogger.getLogger(ClassDepot.class.getName());

  private ClassDepot() {
    templates = new TreeMap<String, ClassTemplate>();
  }

  private ClassTemplate getOrCreateTemplate(String cName, Class clazz) {
    ClassTemplate ct = templates.get(cName);
    if (ct != null) return ct;
    //System.out.println("Adding to parents of "+cName);
    ct = new ClassTemplate(clazz);
    templates.put(cName, ct);
    Class parent = clazz.getSuperclass();
    if (parent != null) {
      ClassTemplate pt = getOrCreateTemplate(parent.getName(), parent);
      ct.addFields(pt);
    }
    return ct;
  }

  public int getFieldIndex(String cName, String field) {
    try {
      Class clazz = Class.forName(cName);
      ClassTemplate ct = getOrCreateTemplate(cName, clazz);
      return ct.getFieldIndex(field);
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "", e);
      System.exit(-1);
    }
    return -1;
  }

  public int getStaticFieldIndex(String cName, String field) {
    try {
      Class clazz = Class.forName(cName);
      ClassTemplate ct = getOrCreateTemplate(cName, clazz);
      return ct.getStaticFieldIndex(field);
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "", e);
      System.exit(-1);
    }
    return -1;
  }

  public int nFields(String cName) {
    try {
      Class clazz = Class.forName(cName);
      ClassTemplate ct = getOrCreateTemplate(cName, clazz);
      return ct.nFields();
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "", e);
      System.exit(-1);
    }
    return -1;
  }

  public int nStaticFields(String cName) {
    try {
      Class clazz = Class.forName(cName);
      ClassTemplate ct = getOrCreateTemplate(cName, clazz);
      return ct.nStaticFields();
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "", e);
      System.exit(-1);
    }
    return -1;
  }

  public static void main(String[] args) {
    System.out.println(instance.getFieldIndex("janala.scratchpad.B1", "x"));
  }

  public int getClassId(String className) {
    return 0; //To change body of created methods use File | Settings | File Templates.
  }
}