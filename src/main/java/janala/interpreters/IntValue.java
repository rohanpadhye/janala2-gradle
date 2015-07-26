package janala.interpreters;


import janala.solvers.History;
import java.util.Map;

public class IntValue extends Value {
  public SymbolicInt symbolic;
  public Constraint nonIntConstraint;
  public int concrete;

  final public static IntValue TRUE = new IntValue(1);
  final public static IntValue FALSE = new IntValue(0);

  @Override
  public Object getConcrete() {
    return concrete;
  }

  public IntValue(int i) {
    concrete = i;
    symbolic = null;
    nonIntConstraint = null;
  }

  public IntValue(int concrete, Constraint nonIntConstraint) {
    this.concrete = concrete;
    if (nonIntConstraint instanceof SymbolicInt) {
      this.symbolic = (SymbolicInt) nonIntConstraint;
    } else {
      this.nonIntConstraint = nonIntConstraint;
    }
  }

  public int getSymbol() {
    if (symbolic == null) {
      throw new RuntimeException("No symbols created.");
    }
    Integer[] result = symbolic.getLinear().keySet().toArray(new Integer[0]);
    return result[0];
  }

  @Override
  public int MAKE_SYMBOLIC(History history) {
    symbol = symbol + inc;
    symbolic = new SymbolicInt(symbol - inc);
    return symbol - inc;
  }

  public long substituteInLinear(Map<String, Long> assignments) {
    long val = 0;

    if (symbolic == null) {
      return concrete;
    }
    for (Map.Entry<Integer, Long> it : symbolic.getLinear().entrySet()) {
      int key = it.getKey();
      long l = it.getValue();
      if (assignments.containsKey("x" + key)) {
        val += assignments.get("x" + key) * l;
      } else {
        return this.concrete;
      }
    }
    val += symbolic.getConstant();
    return val;
  }

  public IntValue IINC(int increment) {
    IntValue ret = new IntValue(concrete + increment);
    if (symbolic != null) {
      ret.symbolic = symbolic.add(increment);
    }
    return ret;
  }

  public IntValue IFEQ() {
    boolean result = concrete == 0;
    if (symbolic == null && nonIntConstraint == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null) {
      if (symbolic.getOp() == SymbolicInt.COMPARISON_OPS.UN)
        return new IntValue(
            result ? 1 : 0,
            result
                ? symbolic.setop(SymbolicInt.COMPARISON_OPS.EQ)
                : symbolic.setop(SymbolicInt.COMPARISON_OPS.NE));
      else return new IntValue(result ? 1 : 0, result ? (SymbolicInt) symbolic.not() : symbolic);
    } else {
      return new IntValue(result ? 1 : 0, result ? nonIntConstraint.not() : nonIntConstraint);
    }
  }

  public IntValue IFNE() {
    boolean result = concrete != 0;
    if (symbolic == null && nonIntConstraint == null) {
      return (concrete != 0) ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null) {
      if (symbolic.getOp() == SymbolicInt.COMPARISON_OPS.UN)
        return new IntValue(
            result ? 1 : 0,
            result
                ? symbolic.setop(SymbolicInt.COMPARISON_OPS.NE)
                : symbolic.setop(SymbolicInt.COMPARISON_OPS.EQ));
      else return new IntValue(result ? 1 : 0, result ? symbolic : (SymbolicInt) symbolic.not());
    } else {
      return new IntValue(result ? 1 : 0, result ? nonIntConstraint : nonIntConstraint.not());
    }
  }

  public IntValue IFLT() {
    if (symbolic == null) {
      return (concrete < 0) ? IntValue.TRUE : IntValue.FALSE;
    } else {
      boolean result = concrete < 0;
      return new IntValue(
          result ? 1 : 0,
          result
              ? symbolic.setop(SymbolicInt.COMPARISON_OPS.LT)
              : symbolic.setop(SymbolicInt.COMPARISON_OPS.GE));
    }
  }

  public IntValue IFGE() {
    if (symbolic == null) {
      return (concrete >= 0) ? IntValue.TRUE : IntValue.FALSE;
    } else {
      boolean result = concrete >= 0;
      return new IntValue(
          result ? 1 : 0,
          result
              ? symbolic.setop(SymbolicInt.COMPARISON_OPS.GE)
              : symbolic.setop(SymbolicInt.COMPARISON_OPS.LT));
    }
  }

  public IntValue IFGT() {
    if (symbolic == null) {
      return (concrete > 0) ? IntValue.TRUE : IntValue.FALSE;
    } else {
      boolean result = concrete > 0;
      return new IntValue(
          result ? 1 : 0,
          result
              ? symbolic.setop(SymbolicInt.COMPARISON_OPS.GT)
              : symbolic.setop(SymbolicInt.COMPARISON_OPS.LE));
    }
  }

  public IntValue IFLE() {
    if (symbolic == null) {
      return (concrete <= 0) ? IntValue.TRUE : IntValue.FALSE;
    } else {
      boolean result = concrete <= 0;
      return new IntValue(
          result ? 1 : 0,
          result
              ? symbolic.setop(SymbolicInt.COMPARISON_OPS.LE)
              : symbolic.setop(SymbolicInt.COMPARISON_OPS.GT));
    }
  }

  public IntValue IF_ICMPEQ(IntValue i2) {
    boolean result = (concrete == i2.concrete);
    SymbolicInt.COMPARISON_OPS op =
        result ? SymbolicInt.COMPARISON_OPS.EQ : SymbolicInt.COMPARISON_OPS.NE;
    if (symbolic == null && i2.symbolic == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null && i2.symbolic != null) {
      SymbolicInt tmp = symbolic.subtract(i2.symbolic);
      if (tmp != null) tmp = tmp.setop(op);
      else tmp = null;
      IntValue ret = new IntValue(result ? 1 : 0, tmp);

      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(result ? 1 : 0, symbolic.subtract(i2.concrete).setop(op));
      return ret;
    } else {
      IntValue ret = new IntValue(result ? 1 : 0, i2.symbolic.subtractFrom(concrete).setop(op));
      return ret;
    }
  }

  public IntValue IF_ICMPNE(IntValue i2) {
    boolean result = (concrete != i2.concrete);
    SymbolicInt.COMPARISON_OPS op =
        result ? SymbolicInt.COMPARISON_OPS.NE : SymbolicInt.COMPARISON_OPS.EQ;
    if (symbolic == null && i2.symbolic == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null && i2.symbolic != null) {
      SymbolicInt tmp = symbolic.subtract(i2.symbolic);
      if (tmp != null) tmp = tmp.setop(op);
      else tmp = null;
      IntValue ret = new IntValue(result ? 1 : 0, tmp);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(result ? 1 : 0, symbolic.subtract(i2.concrete).setop(op));
      return ret;
    } else {
      IntValue ret = new IntValue(result ? 1 : 0, i2.symbolic.subtractFrom(concrete).setop(op));
      return ret;
    }
  }

  public IntValue IF_ICMPLT(IntValue i2) {
    boolean result = (concrete < i2.concrete);
    SymbolicInt.COMPARISON_OPS op =
        result ? SymbolicInt.COMPARISON_OPS.LT : SymbolicInt.COMPARISON_OPS.GE;
    if (symbolic == null && i2.symbolic == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null && i2.symbolic != null) {
      SymbolicInt tmp = symbolic.subtract(i2.symbolic);
      if (tmp != null) tmp = tmp.setop(op);
      else tmp = null;
      IntValue ret = new IntValue(result ? 1 : 0, tmp);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(result ? 1 : 0, symbolic.subtract(i2.concrete).setop(op));
      return ret;
    } else {
      IntValue ret = new IntValue(result ? 1 : 0, i2.symbolic.subtractFrom(concrete).setop(op));
      return ret;
    }
  }

  public IntValue IF_ICMPGE(IntValue i2) {
    boolean result = (concrete >= i2.concrete);
    SymbolicInt.COMPARISON_OPS op =
        result ? SymbolicInt.COMPARISON_OPS.GE : SymbolicInt.COMPARISON_OPS.LT;
    if (symbolic == null && i2.symbolic == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null && i2.symbolic != null) {
      SymbolicInt tmp = symbolic.subtract(i2.symbolic);
      if (tmp != null) tmp = tmp.setop(op);
      else tmp = null;
      IntValue ret = new IntValue(result ? 1 : 0, tmp);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(result ? 1 : 0, symbolic.subtract(i2.concrete).setop(op));
      return ret;
    } else {
      IntValue ret = new IntValue(result ? 1 : 0, i2.symbolic.subtractFrom(concrete).setop(op));
      return ret;
    }
  }

  public IntValue IF_ICMPGT(IntValue i2) {
    boolean result = (concrete > i2.concrete);
    SymbolicInt.COMPARISON_OPS op =
        result ? SymbolicInt.COMPARISON_OPS.GT : SymbolicInt.COMPARISON_OPS.LE;
    if (symbolic == null && i2.symbolic == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null && i2.symbolic != null) {
      SymbolicInt tmp = symbolic.subtract(i2.symbolic);
      if (tmp != null) tmp = tmp.setop(op);
      else tmp = null;
      IntValue ret = new IntValue(result ? 1 : 0, tmp);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(result ? 1 : 0, symbolic.subtract(i2.concrete).setop(op));
      return ret;
    } else {
      IntValue ret = new IntValue(result ? 1 : 0, i2.symbolic.subtractFrom(concrete).setop(op));
      return ret;
    }
  }

  public IntValue IF_ICMPLE(IntValue i2) {
    boolean result = (concrete <= i2.concrete);
    SymbolicInt.COMPARISON_OPS op =
        result ? SymbolicInt.COMPARISON_OPS.LE : SymbolicInt.COMPARISON_OPS.GT;
    if (symbolic == null && i2.symbolic == null) {
      return result ? IntValue.TRUE : IntValue.FALSE;
    } else if (symbolic != null && i2.symbolic != null) {
      SymbolicInt tmp = symbolic.subtract(i2.symbolic);
      if (tmp != null) tmp = tmp.setop(op);
      else tmp = null;
      IntValue ret = new IntValue(result ? 1 : 0, tmp);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(result ? 1 : 0, symbolic.subtract(i2.concrete).setop(op));
      return ret;
    } else {
      IntValue ret = new IntValue(result ? 1 : 0, i2.symbolic.subtractFrom(concrete).setop(op));
      return ret;
    }
  }

  public IntValue IADD(IntValue i) {
    if (symbolic == null && i.symbolic == null) {
      return new IntValue(concrete + i.concrete);
    } else if (symbolic != null && i.symbolic != null) {
      IntValue ret = new IntValue(concrete + i.concrete);
      ret.symbolic = symbolic.add(i.symbolic);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(concrete + i.concrete);
      ret.symbolic = symbolic.add(i.concrete);
      return ret;
    } else {
      IntValue ret = new IntValue(concrete + i.concrete);
      ret.symbolic = i.symbolic.add(concrete);
      return ret;
    }
  }

  public IntValue ISUB(IntValue i) {
    if (symbolic == null && i.symbolic == null) {
      return new IntValue(concrete - i.concrete);
    } else if (symbolic != null && i.symbolic != null) {
      IntValue ret = new IntValue(concrete - i.concrete);
      ret.symbolic = symbolic.subtract(i.symbolic);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(concrete - i.concrete);
      ret.symbolic = symbolic.subtract(i.concrete);
      return ret;
    } else {
      IntValue ret = new IntValue(concrete - i.concrete);
      ret.symbolic = i.symbolic.subtractFrom(concrete);
      return ret;
    }
  }

  public IntValue IMUL(IntValue i) {
    if (symbolic == null && i.symbolic == null) {
      return new IntValue(concrete * i.concrete);
    } else if (symbolic != null && i.symbolic != null) {
      IntValue ret = new IntValue(concrete * i.concrete);
      ret.symbolic = symbolic.multiply(i.concrete);
      return ret;
    } else if (symbolic != null) {
      IntValue ret = new IntValue(concrete * i.concrete);
      ret.symbolic = symbolic.multiply(i.concrete);
      return ret;
    } else {
      IntValue ret = new IntValue(concrete * i.concrete);
      ret.symbolic = i.symbolic.multiply(concrete);
      return ret;
    }
  }

  public IntValue IDIV(IntValue i) {
    return new IntValue(concrete / i.concrete);
  }

  public IntValue IREM(IntValue i) {
    return new IntValue(concrete % i.concrete);
  }

  public IntValue INEG() {
    if (symbolic == null) return new IntValue(-concrete);
    else {
      IntValue ret = new IntValue(-concrete);
      ret.symbolic = symbolic.subtractFrom(0);
      return ret;
    }
  }

  public IntValue ISHL(IntValue i) {
    return new IntValue(concrete << i.concrete);
  }

  public IntValue ISHR(IntValue i) {
    return new IntValue(concrete >> i.concrete);
  }

  public IntValue IUSHR(IntValue i) {
    return new IntValue(concrete >>> i.concrete);
  }

  public IntValue IAND(IntValue i) {
    return new IntValue(concrete & i.concrete);
  }

  public IntValue IOR(IntValue i) {
    return new IntValue(concrete | i.concrete);
  }

  public IntValue IXOR(IntValue i) {
    return new IntValue(concrete ^ i.concrete);
  }

  public LongValue I2L() {
    return new LongValue((long) concrete, symbolic);
  }

  public FloatValue I2F() {
    return new FloatValue((float) concrete);
  }

  public DoubleValue I2D() {
    return new DoubleValue((double) concrete);
  }

  public IntValue I2B() {
    return new IntValue((byte) concrete, symbolic);
  }

  public IntValue I2C() {
    return new IntValue((char) concrete, symbolic);
  }

  public IntValue I2S() {
    return new IntValue((short) concrete, symbolic);
  }

  @Override
  public String toString() {
    return "IntValue{" + "symbolic=" + symbolic + ", concrete=" + concrete + '}';
  }

  public Constraint getSymbolic() {
    return symbolic != null ? symbolic : (nonIntConstraint != null ? nonIntConstraint : null);
  }

  public SymbolicInt getSymbolicInt() {
    return symbolic;
  }
}