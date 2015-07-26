package janala.solvers;

import java.util.ArrayList;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 7/9/12
 * Time: 7:21 PM
 */
public class DFSStrategy extends Strategy {
  @Override
  public int solve(ArrayList<Element> history, int historySize, History solver) {
    int j, to = -1, ret;

    for (j = 0; j < historySize; j++) {
      Element tmp = history.get(j);
      BranchElement current;
      if (tmp instanceof BranchElement) {
        current = (BranchElement) tmp;
        if (current.isForceTruth && !current.branch) {
          if ((ret = dfs(history, j, to, solver)) != -1) {
            return ret;
          }
          to = j;
        } else if (current.isForceTruth) {
          to = j;
        }
      }
    }

    if (j >= historySize) {
      j = historySize - 1;
    }

    return dfs(history, j, to, solver);
  }

  private int dfs(ArrayList<Element> history, int from, int to, History solver) {
    for (int i = from; i > to; i--) {
      Element tmp = history.get(i);
      if (tmp instanceof BranchElement) {
        BranchElement current = (BranchElement) tmp;
        if (!current.done && current.pathConstraintIndex != -1) {
          if (solver.solveAt(current.pathConstraintIndex)) {
            return i;
          }
        }
      }
    }
    return -1;
  }
}