package begyyal.splatoon.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.splatoon.object.ResultTable;

public class ResultTableDao {

    private final Path path;

    private ResultTableDao(Path path) {
	this.path = path;
    }

    public static ResultTableDao newi() throws IOException {
	var pathStr = ResourceBundle.getBundle("common").getString("tablePath");
	var path = Paths.get(pathStr);
	if (!Files.exists(path)) {
	    Path pp = path;
	    var ppl = SuperListGen.<Path>newi();
	    while (!Files.exists(pp = pp.getParent()))
		ppl.add(pp);
	    for (var rpp : ppl.reverse())
		Files.createDirectory(rpp);
	    Files.createFile(path);
	}
	return new ResultTableDao(path);
    }

    public ResultTable read() throws IOException {
	var lines = Files.readAllLines(this.path);
	return ResultTable.of(lines);
    }

    public void write(ResultTable table) throws IOException {
	Files.write(this.path, table.serialize());
    }
}
