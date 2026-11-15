package executor;

import database.Database;
import index.IndexManager;
import parser.ParsedQuery;

/*
Обработчик операций INSERT, SELECT, UPDATE и DELETE.
*/
public class CrudHandler implements QueryExecutor.QueryHandler {

    @Override
    public Object execute(
            ParsedQuery query,
            Database database,
            IndexManager indexManager
    ) {
        throw new UnsupportedOperationException(
                "CRUD-операции пока не реализованы"
        );
    }
}
