package janala.interpreters;

import janala.config.Config;
import janala.solvers.History;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 7/1/12
 * Time: 9:56 AM
 */
public class StaticInvocation {
  public static Value invokeMethod(
      int iid, String owner, String name, Value[] args, History history) {
    if (owner.equals("java/lang/Integer") && name.equals("valueOf")) {
      IntegerObjectValue ret = new IntegerObjectValue();
      if (args[0] instanceof IntValue) {
        ret.intValue = (IntValue) args[0];
        return ret;
      }
    } else if (owner.equals("java/lang/Long") && name.equals("valueOf")) {
      LongObjectValue ret = new LongObjectValue();
      if (args[0] instanceof LongValue) {
        ret.longValue = (LongValue) args[0];
        return ret;
      }
    } else if (owner.equals("java/sql/Date") && name.equals("valueOf")) {
      SqlDateObjectValue ret = new SqlDateObjectValue();
      if (args[0] instanceof StringValue) {
        ret.longValue =
            new LongValue((java.sql.Date.valueOf(((StringValue) args[0]).getConcrete()).getTime()));
        return ret;
      }
    } else if (owner.equals("java/sql/Time") && name.equals("valueOf")) {
      SqlDateObjectValue ret = new SqlDateObjectValue();
      if (args[0] instanceof StringValue) {
        ret.longValue =
            new LongValue((java.sql.Time.valueOf(((StringValue) args[0]).getConcrete()).getTime()));
        return ret;
      }
    } else if (owner.equals("java/sql/Timestamp") && name.equals("valueOf")) {
      SqlDateObjectValue ret = new SqlDateObjectValue();
      if (args[0] instanceof StringValue) {
        ret.longValue =
            new LongValue(
                (java.sql.Timestamp.valueOf(((StringValue) args[0]).getConcrete()).getTime()));
        return ret;
      }
    } else if (owner.equals("janala/Main") && name.equals("Assume") && args.length == 1) {
      if (((IntValue) args[0]).concrete != 0) {
        history.setLastBranchDone();
      }
      return PlaceHolder.instance;
    } else if (owner.equals("janala/Main") && name.equals("ForceTruth") && args.length == 1) {
      history.setLastForceTruth();
      return PlaceHolder.instance;
    } else if (owner.equals("janala/Main") && name.equals("MakeSymbolic") && args.length == 1) {
      int symbol = args[0].MAKE_SYMBOLIC(history);
      history.addInput(symbol, args[0]);
      return PlaceHolder.instance;
    } else if (owner.equals("janala/Main") && name.equals("BeginScope") && args.length == 0) {
      history.addInput(Config.instance.scopeBeginSymbol, null);
      history.beginScope(iid);
      return PlaceHolder.instance;
    } else if (owner.equals("janala/Main") && name.equals("EndScope") && args.length == 0) {
      history.addInput(Config.instance.scopeEndSymbol, null);
      history.endScope(iid);
      return PlaceHolder.instance;
    } else if (owner.equals("janala/Main")
        && name.equals("AbstractEqualsConcrete")
        && args.length == 1) {
      history.abstractData(((IntValue) args[0]).concrete != 0, iid);
      return PlaceHolder.instance;
    } else if (owner.equals("janala/Main") && name.equals("AssumeOrBegin") && args.length == 1) {
      return history.assumeOrBegin((IntValue) args[0]);
    } else if (owner.equals("janala/Main") && name.equals("AssumeOr") && args.length == 2) {
      return history.assumeOr((IntValue) args[0], (SymbolicOrValue) args[1]);
    } else if (owner.equals("janala/Main") && name.equals("AssumeOrEnd") && args.length == 1) {
      return history.assumeOrEnd(iid, (SymbolicOrValue) args[0]);
     } else if (owner.equals("janala/Main") && name.equals("Ignore") && args.length == 0) {
      return history.ignore();
      //return PlaceHolder.instance;
    }

    return PlaceHolder.instance;
  }
}
