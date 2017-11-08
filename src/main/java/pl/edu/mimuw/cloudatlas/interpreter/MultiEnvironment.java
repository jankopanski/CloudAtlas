package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.ValueNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiEnvironment extends Environment {
    private final List<TableRow> rows;
    private final Map<String, Integer> columns = new HashMap<String, Integer>();

    public MultiEnvironment(Iterable<TableRow> table, List<String> columns) {
        super(null, columns); //TODO fix it
        this.rows = new ArrayList<>();
        for (TableRow row : table)
            this.rows.add(row);
        int i = 0;
        for(String c : columns)
            this.columns.put(c, i++);
    }

    public Result getIdent(String ident) {
        if (columns.get(ident) == null)
            throw new InternalInterpreterException("Attribute not found in child nodes");
        return new ResultColumn(rows.stream().map(r -> new ResultSingle(r.getIth(columns.get(ident)))).collect(Collectors.toList()));
    }

}
