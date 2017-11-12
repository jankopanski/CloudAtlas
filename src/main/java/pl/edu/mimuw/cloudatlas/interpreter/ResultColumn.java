package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResultColumn extends Result {

//    private final List<ResultSingle> values;
    private final ValueList values;

    public ResultColumn(List<Value> values) {
        this.values = new ValueList(values, TypeCollection.computeElementType(values));
    };

    protected ResultColumn binaryOperationTyped(BinaryOperation operation, ResultColumn right) {
        List<Value> result = new ArrayList<>();
        if (values.size() != right.values.size())
            throw new UnsupportedOperationException("Results binary operation on different lengths");
        for (int i = 0; i < values.size(); ++i)
            result.add(new ResultSingle(values.get(i)).binaryOperationTyped(operation, new ResultSingle(right.values.get(i))).getValue());
        return new ResultColumn(result);
    }

    @Override
    public ResultColumn binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
        return new ResultColumn(values.stream().map(r -> (new ResultSingle(r)).binaryOperationTyped(operation, right).getValue()).collect(Collectors.toList()));
    }


    @Override
    public ResultColumn unaryOperation(final UnaryOperation operation) {
        return new ResultColumn(values.stream().map( (r) -> (new ResultSingle(r)).unaryOperation(operation).getValue()).collect(Collectors.toList()));
    }

    @Override
    public ValueList getList() {
        throw new UnsupportedOperationException("Not a ResultList."); //TODO dodać unfold
    }

    @Override
    public Result callMe(BinaryOperation operation, Result left) { return left.binaryOperationTyped(operation, this); }

    @Override
    public ValueList getColumn() {
        return values;
    }

    public ResultSingle aggregationOperation(AggregationOperation operation) {
        return new ResultSingle(operation.perform(this.getColumn()));
    }

    public Result transformOperation(TransformOperation operation) {
        return new ResultColumn(operation.perform(values));
    }


    @Override
    public Value getValue() {
        return values;
    }

    @Override
    public Type getType() {
        return TypeCollection.computeElementType(values);
    }

    @Override
    public Result random(int size) {
        List<Value> list = new ArrayList<>();
        list.addAll(values);
        if (size >= list.size())
            return new ResultColumn(list);
        Collections.shuffle(list);
        return new ResultColumn(list.subList(0, size));
    }

    @Override
    public Result filterNulls() {
        List<Value> filtered = values.stream().filter(Value::isNull).collect(Collectors.toList());
        if (filtered.isEmpty())
            return new ResultSingle(null);
        else
        return new ResultColumn(filtered);
    }

    @Override
    public Result first(int size) {
        if (size > values.size())
            size = values.size();
        List<Value> l = values.subList(0, size);
        return new ResultSingle(new ValueList(l, TypeCollection.computeElementType(l)));
    }

    @Override
    public Result last(int size) {
        if (size > values.size())
            size = values.size();
        List<Value> l = values.subList(values.size() - size, values.size());
        return new ResultSingle(new ValueList(l, TypeCollection.computeElementType(l)));
    }

    @Override
    public Result convertTo(Type to) {
      return new ResultColumn(values.stream().map((r) -> r.convertTo(to)).collect(Collectors.toList()));
    }

    @Override
    public ResultSingle isNull() {
        return new ResultSingle(new ValueBoolean(values == null));
    }
}

