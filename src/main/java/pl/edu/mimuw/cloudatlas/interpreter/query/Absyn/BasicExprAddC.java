package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class BasicExprAddC extends BasicExpr {
  public final BasicExpr basicexpr_1, basicexpr_2;

  public BasicExprAddC(BasicExpr p1, BasicExpr p2) { basicexpr_1 = p1; basicexpr_2 = p2; }

  public <R,A> R accept(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExpr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprAddC) {
      pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprAddC x = (pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExprAddC)o;
      return this.basicexpr_1.equals(x.basicexpr_1) && this.basicexpr_2.equals(x.basicexpr_2);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.basicexpr_1.hashCode())+this.basicexpr_2.hashCode();
  }


}
