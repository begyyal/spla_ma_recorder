package begyyal.splatoon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import begyyal.splatoon.object.ResultTable;

public class ResultTableDao {

    private final Path path;

    private ResultTableDao() throws IOException {
	var pathStr = ResourceBundle.getBundle("common").getString("tablePath");
	this.path = Paths.get(pathStr);
	if (!Files.exists(this.path))
	    Files.createFile(this.path);
    }

    public static ResultTableDao newi() throws IOException {
	return new ResultTableDao();
    }

    public ResultTable read() throws IOException {
	var lines = Files.readAllLines(this.path);
	return ResultTable.of(lines);
    }

    public void write(ResultTable table) throws IOException {
	Files.write(this.path, table.serialize());
    }
}
