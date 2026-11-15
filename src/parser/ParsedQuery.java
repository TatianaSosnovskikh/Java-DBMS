package parser;
import database.Row;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParsedQuery{
    public enum QueryType {
        INSERT, SELECT, UPDATE, DELETE, CREATE_TABLE, DROP_TABLE, CREATE_INDEX
    }
    private QueryType queryType;
    private String tableName;
    private List<String> columns = new ArrayList<>();
    private List<String> values = new ArrayList<>();
    private List<List<String>> rowsValues = new ArrayList<>();
    private Map<String, String> assignments = new LinkedHashMap<>();
    private String conditionColumn;
    private String conditionOperator;
    private String conditionValue;
    private String indexName;
    private String indexedColumn;
    private List<String> columnDefinitions = new ArrayList<>();
    private List<Row> candidateRows = new ArrayList<>();
    private boolean indexUsed;
    public QueryType getQueryType() {
        return queryType;
    }
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }
    public List<List<String>> getRowsValues() {
        return rowsValues;
    }
    public void setRowsValues(List<List<String>> rowsValues) {
        this.rowsValues = rowsValues;
    }
    public Map<String, String> getAssignments() {
        return assignments;
    }
    public void setAssignments(Map<String, String> assignments) {
        this.assignments = assignments;
    }
    public String getConditionColumn() {
        return conditionColumn;
    }
    public void setConditionColumn(String conditionColumn) {
        this.conditionColumn = conditionColumn;
    }
    public String getConditionOperator() {
        return conditionOperator;
    }
    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }
    public String getConditionValue() {
        return conditionValue;
    }
    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }
    public String getIndexName() {
        return indexName;
    }
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    public String getIndexedColumn() {
        return indexedColumn;
    }
    public void setIndexedColumn(String indexedColumn) {
        this.indexedColumn = indexedColumn;
    }
    public List<String> getColumnDefinitions() {
        return columnDefinitions;
    }
    public void setColumnDefinitions(List<String> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }
    public List<Row> getCandidateRows() {
        return candidateRows;
    }
    public void setCandidateRows(List<Row> candidateRows) {
        this.candidateRows = candidateRows;
    }
    public boolean isIndexUsed() {
        return indexUsed;
    }
    public void setIndexUsed(boolean indexUsed) {
        this.indexUsed = indexUsed;
    }
}