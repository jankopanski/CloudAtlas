package pl.edu.mimuw.cloudatlas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data @AllArgsConstructor
public class ValueCert extends Value {
    private Attribute name;
    private String query;
    private List<Attribute> attributes;

    @Override
    public Type getType() {
        return TypePrimitive.CERT;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Value convertTo(Type to) {
        return null;
    }

    @Override
    public Value getDefaultValue() {
        return null;
    }
}
