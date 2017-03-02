package janala.logger.inst;

public class METHOD_BEGIN extends Instruction {
  private String owner;
  private String name;
  private String desc;

  public METHOD_BEGIN(String owner, String name, String desc) {
    super(-1, -1);
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  public void visit(IVisitor visitor) {
    visitor.visitMETHOD_BEGIN(this);
  }

  @Override
  public String toString() {
    return "METHOD_BEGIN"
            + " owner="
            + owner
            + " name="
            + name
            + " desc="
            + desc;
  }
}
