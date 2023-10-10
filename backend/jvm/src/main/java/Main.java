import static java.util.FormatProcessor.FMT;

int count = 10;

public sealed interface Expr {
    record Add(Expr left, Expr right) implements Expr {
    }

    record Mul(Expr left, Expr right) implements Expr {
    }

    record Div(Expr left, Expr right) implements Expr {
    }

    record Neg(Expr e) implements Expr {
    }

    sealed interface Const extends Expr {
        record Int(int i) implements Const {
        }

        record Double(double d) implements Const {
        }

        record Long(long l) implements Const {
        }

        record Str(String s) implements Const {
        }
    }
}


long eval(Expr expr) {
    return switch (expr) {
        case Expr.Add(var l, var r) -> eval(l) + eval(r);
        case Expr.Mul(var l, var r) -> eval(l) * eval(r);
        case Expr.Div(var l, var r) -> eval(l) / eval(r);
        case Expr.Neg(var e) -> -eval(e);
        case Expr.Const c -> switch (c) {
            case Expr.Const.Int(var i) -> i;
            case Expr.Const.Double(var d) -> (long) d;
            case Expr.Const.Long(var l) -> l;
            case Expr.Const.Str(var s) -> Long.parseLong(s);
        };
    };
}


void main() {
    Expr expr = new Expr.Add(new Expr.Const.Int(7), new Expr.Const.Long(3));
    expr = new Expr.Div(expr, new Expr.Const.Int(2));
    expr = new Expr.Add(expr, new Expr.Const.Double(5.0));
    String user = FMT."""
       %05d\{count}
       Eval(\{expr}) = \{eval(expr)}
       """.stripIndent();
    System.out.println(user);
}
