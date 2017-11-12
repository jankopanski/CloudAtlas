package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.ValueNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiEnvironment implements Environment {
    Table table;

    public MultiEnvironment(Table table) {
        this.table = table;
    }

    public Result getIdent(String ident) {
        return new ResultColumn(table.getColumn(ident));
    }
}
