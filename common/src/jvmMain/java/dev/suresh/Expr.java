package dev.suresh;

import org.jspecify.annotations.NullMarked;

@NullMarked
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

    static long eval(Expr expr) {
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
}