package janala.solvers

import static org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.Before
import janala.interpreters.SymbolicInt
import janala.interpreters.SymOrInt
import janala.interpreters.SymbolicAndConstraint
import janala.interpreters.SymbolicNotConstraint
import janala.interpreters.SymbolicOrConstraint
import janala.interpreters.SymbolicFalseConstraint
import janala.interpreters.SymbolicTrueConstraint
import janala.interpreters.SymbolicIntCompareConstraint
import janala.interpreters.SymbolicStringPredicate
import janala.interpreters.SymbolicStringPredicate.STRING_COMPARISON_OPS
import janala.interpreters.COMPARISON_OPS
import janala.interpreters.Constraint

import groovy.transform.CompileStatic

@CompileStatic
class CVC4SolverTest {
  CVC4Solver solver

  @Before
  void setup() {
    solver = new CVC4Solver()
  }

  private void testSymbolicInt(SymbolicInt y, COMPARISON_OPS op, String expected) {
    y.setOp(op)
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print((Constraint)y)
    assertEquals(expected, bytes.toString())
  }

  @Test
  void testPrintSymbolicInt() {
    SymbolicInt x1 = new SymbolicInt(1)
    SymbolicInt x2 = new SymbolicInt(2)
    SymbolicInt y = x1.add(x2).add(1)
    testSymbolicInt(y, COMPARISON_OPS.EQ, "x1*(1) + x2*(1) + (1) = 0")
    testSymbolicInt(y, COMPARISON_OPS.NE, "x1*(1) + x2*(1) + (1) /= 0")
    testSymbolicInt(y, COMPARISON_OPS.GT, "x1*(1) + x2*(1) + (1) > 0")
    testSymbolicInt(y, COMPARISON_OPS.LT, "x1*(1) + x2*(1) + (1) < 0")
    testSymbolicInt(y, COMPARISON_OPS.GE, "x1*(1) + x2*(1) + (1) >= 0")
    testSymbolicInt(y, COMPARISON_OPS.LE, "x1*(1) + x2*(1) + (1) <= 0")
  }

  private void testSymbolicCompareInt(SymOrInt x, SymOrInt y, COMPARISON_OPS op, String expected) {
    Constraint con = new SymbolicIntCompareConstraint(x, y, op)
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print(con)
    assertEquals(expected, bytes.toString())
  }

  @Test
  void testPrintSymbolicCompareInt() {
    SymOrInt x1 = new SymOrInt("x_1")
    SymOrInt x2 = new SymOrInt(1)
    testSymbolicCompareInt(x1, x2, COMPARISON_OPS.EQ, "(x_1) - (1) = 0")
    testSymbolicCompareInt(x1, x2, COMPARISON_OPS.NE, "(x_1) - (1) /= 0")
    testSymbolicCompareInt(x1, x2, COMPARISON_OPS.GT, "(x_1) - (1) > 0")
    testSymbolicCompareInt(x1, x2, COMPARISON_OPS.LT, "(x_1) - (1) < 0")
    testSymbolicCompareInt(x1, x2, COMPARISON_OPS.GE, "(x_1) - (1) >= 0")
    testSymbolicCompareInt(x1, x2, COMPARISON_OPS.LE, "(x_1) - (1) <= 0")
  }

  @Test
  void testPrintSymbolicOrConstraint() {
    SymbolicInt x1 = new SymbolicInt(1)
    x1.setOp(COMPARISON_OPS.EQ)
    SymbolicInt x2 = new SymbolicInt(2)
    x2.setOp(COMPARISON_OPS.EQ)
    Constraint con = new SymbolicOrConstraint(x1).OR(x2)
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print(con)
    assertEquals("(x1*(1) = 0) OR (x2*(1) = 0)", bytes.toString())
  }

  @Test
  void testPrintSymbolicAndConstraint() {
    SymbolicInt x1 = new SymbolicInt(1)
    x1.setOp(COMPARISON_OPS.EQ)
    SymbolicInt x2 = new SymbolicInt(2)
    x2.setOp(COMPARISON_OPS.EQ)
    Constraint con = new SymbolicAndConstraint(x1).AND(x2)
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print(con)
    assertEquals("(x1*(1) = 0) AND (x2*(1) = 0)", bytes.toString())
  }

  @Test
  void testPrintSymbolicNotConstraint() {
    SymbolicInt x1 = new SymbolicInt(1)
    x1.setOp(COMPARISON_OPS.EQ)
    Constraint con = new SymbolicNotConstraint(x1)
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print(con)
    assertEquals(" NOT (x1*(1) = 0)", bytes.toString())
  }

  @Test
  void testPrintSymbolicTrueConstraint() {
    Constraint con = SymbolicTrueConstraint.instance
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print(con)
    assertEquals(" TRUE ", bytes.toString())
  }

  @Test
  void testPrintSymbolicFalseConstraint() {
    Constraint con = SymbolicFalseConstraint.instance
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.INT, new PrintStream(bytes))
    printer.print(con)
    assertEquals(" FALSE ", bytes.toString())
  }

  @Test
  void testPrintSymbolicStringPredicate() {
    SymbolicStringPredicate con = new SymbolicStringPredicate(
      STRING_COMPARISON_OPS.EQ, "a", "b")
    def bytes = new ByteArrayOutputStream()
    def printer = new CVC4Solver.Printer(new HashSet<String>(), 
      new HashMap<String, Long>(), CVC4Solver.CONSTRAINT_TYPE.STR, new PrintStream(bytes))
    printer.print(con)
    assertEquals("((97) - (98) = 0)", bytes.toString())
  }
}