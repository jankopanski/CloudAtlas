package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResultColumn extends Result {

    private final List<ResultSingle> values;

    public ResultColumn(List<ResultSingle> values) {
        this.values = values;
    };

    protected ResultColumn binaryOperationTyped(BinaryOperation operation, ResultColumn right) {
        List<ResultSingle> result = new ArrayList<>();
        if (values.size() != right.values.size())
            throw new UnsupportedOperationException("Results binary operation on different lengths");
        for (int i = 0; i < values.size(); ++i)
            result.add(values.get(i).binaryOperationTyped(operation, right.values.get(i)));
        return new ResultColumn(result);
    }

    @Override
    public ResultColumn binaryOperationTyped(BinaryOperation operation, ResultSingle right) {
        return new ResultColumn(values.stream().map( (r) -> r.binaryOperationTyped(operation, right)).collect(Collectors.toList()));
    }


    @Override
    public ResultColumn unaryOperation(final UnaryOperation operation) {
        return new ResultColumn(values.stream().map( (r) -> r.unaryOperation(operation)).collect(Collectors.toList()));
    }

    @Override
    public ValueList getList() {
        throw new UnsupportedOperationException("Not a ResultList."); //TODO sprawdzić czy z unfoldem lepiej
    }

    @Override
    public Result callMe(BinaryOperation operation, Result left) { return left.binaryOperationTyped(operation, this); }

    @Override
    public ValueList getColumn() {
        List<Value> list = values.stream().map(ResultSingle::getValue).collect(Collectors.toList());
        return new ValueList(list, TypeCollection.computeElementType(list));
    }

    public ResultSingle aggregationOperation(AggregationOperation operation) {
        return new ResultSingle(operation.perform(this.getColumn()));
    }

    public Result transformOperation(TransformOperation operation) {
        List<Value> results = operation.perform(this.getColumn()).getValue();
        return new ResultColumn(results.stream().map(ResultSingle::new).collect(Collectors.toList()));
    }


    @Override
    public Value getValue() {
        return getColumn();
    }

    @Override
    public Type getType() {
        return TypeCollection.computeElementType(values.stream().map(ResultSingle::getValue).collect(Collectors.toList()));
    }

    @Override
    public Result random(int size) {
        List<ResultSingle> list = new ArrayList<>();
        list.addAll(values);
        if (size >= list.size())
            return new ResultColumn(list);
        Collections.shuffle(list);
        return new ResultColumn(list.subList(0, size));
    }

    @Override
    public Result filterNulls() {
        List<ResultSingle> filtered = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (filtered.isEmpty())
            return new ResultSingle(null);
        else
        return new ResultColumn(filtered);
    }

    @Override
    public Result first(int size) {
        if (size > values.size())
            size = values.size();
        List<Value> l = values.subList(0, size).stream().map(ResultSingle::getValue).collect(Collectors.toList());
        return new ResultSingle(new ValueList(l, TypeCollection.computeElementType(l)));
    }

    @Override
    public Result last(int size) {
        if (size > values.size())
            size = values.size();
        List<Value> l = values.subList(values.size() - size, values.size()).stream().map(ResultSingle::getValue).collect(Collectors.toList());
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

