package com.imooc.download.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 因为按条件查询 分组 排序等查询的操作稍微复杂
 * 所以单独为查询做了一个支持类 其实也比较简单
 * 主要用的就是builder设计模式
 * @param <T>
 */
public class QuerySupport<T> {

    private static final String TAG = "QuerySupport";
    // 查询的列
    private String[] mQueryColumns;
    // 查询的条件
    private String mQuerySelection;
    // 查询的参数
    private String[] mQuerySelectionArgs;
    // 查询分组
    private String mQueryGroupBy;
    // 查询对结果集进行过滤
    private String mQueryHaving;
    // 查询排序
    private String mQueryOrderBy;
    // 查询可用于分页
    private String mQueryLimit;

    private final Class<T> mClazz;
    private final SQLiteDatabase mSQLiteDatabase;

    public QuerySupport(SQLiteDatabase sqLiteDatabase,Class clazz) {
        this.mClazz = clazz;
        this.mSQLiteDatabase = sqLiteDatabase;
    }
    public QuerySupport<T> columns(String... columns) {
        this.mQueryColumns = columns;
        return this;
    }

    public QuerySupport<T> selectionArgs(String... selectionArgs) {
        this.mQuerySelectionArgs = selectionArgs;
        return this;
    }

    public QuerySupport<T> having(String having) {
        this.mQueryHaving = having;
        return this;
    }

    public QuerySupport<T> orderBy(String orderBy) {
        this.mQueryOrderBy = orderBy;
        return this;
    }

    public QuerySupport<T> limit(String limit) {
        this.mQueryLimit = limit;
        return this;
    }

    public QuerySupport<T> groupBy(String groupBy) {
        this.mQueryGroupBy = groupBy;
        return this;
    }

    public QuerySupport<T> selection(String selection) {
        this.mQuerySelection = selection;
        return this;
    }
    public List<T> query() {
        Cursor cursor =mSQLiteDatabase.query(DaoUtil.getTableName(mClazz),
                mQueryColumns, mQuerySelection,
                mQuerySelectionArgs, mQueryGroupBy, mQueryHaving, mQueryOrderBy, mQueryLimit);
        clearQueryParams();
        return cursorToListByReflect(cursor);
    }
    private void clearQueryParams() {
        mQueryColumns = null;
        mQuerySelection = null;
        mQuerySelectionArgs = null;
        mQueryGroupBy = null;
        mQueryHaving = null;
        mQueryOrderBy = null;
        mQueryLimit = null;
    }
    /**
     * 查询所有数据
     */
    private List<T> cursorToListByReflect(Cursor cursor) {
        List<T> list = new ArrayList<>();
        while (cursor.moveToNext()){
            T instance=null;
            try{
                // 反射new 对象
                instance = mClazz.newInstance();
                Field[] fields = mClazz.getDeclaredFields();

                for (Field field : fields) {
                    Object value = null;
                    field.setAccessible(true);
                    // 以Person为例 它有个属性String name， fieldName 则是 name
                    String name = field.getName();
                    // 查询当前属性在数据库中所在的列 下面则相当于调用cursor.getColumnIndex("name")
                    int index = cursor.getColumnIndex(name);
                    if (index != -1){
                        // 通过反射获取 游标的方法  field.getType() -> 获取的类型
                        Method method = cursorMethod(field.getType());
                        value = method.invoke(cursor, index);
                        if (value==null){
                            continue;
                        }
                        //处理一些特殊的部分
                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            if ("0".equals(String.valueOf(value))) {
                                value = false;
                            } else if ("1".equals(String.valueOf(value))) {
                                value = true;
                            }
                        } else if (field.getType() == char.class || field.getType() == Character.class) {
                            value = ((String) value).charAt(0);
                        } else if (field.getType() == Date.class) {
                            long date = (Long) value;
                            if (date <= 0) {
                                value = null;
                            } else {
                                value = new Date(date);
                            }
                        }
                    }else{
                        Log.e(TAG, "cursorToList: 该属性没有存储在数据库中");
                        continue;
                    }
                    // 反射注入属性的值(以person为例,类似调用person.setName(value))
                    // 第五次反射
                    field.set(instance, value);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            // 添加解析对象
            list.add(instance);
        }
        cursor.close();
        return list;
    }
    /**
     * 通过反射获取 游标的方法  field.getType() -> 获取的类型
     * @param type
     * @return
     */
    private Method cursorMethod(Class<?> type) throws NoSuchMethodException {
        String methodName = getColumnMethodName(type);
        //type String getString(index); int getInt; boolean getBoolean
        Method method = Cursor.class.getMethod(methodName,int.class);
        return method;
    }
    private String getColumnMethodName(Class<?> fieldType) {
        String typeName;
        if (fieldType.isPrimitive()){
            typeName = DaoUtil.capitalize(fieldType.getName());
        }else{
            typeName = fieldType.getSimpleName();
        }
        String methodName = "get"+typeName;
        if ("getBoolean".equals(methodName)) {
            methodName = "getInt";
        } else if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            methodName = "getString";
        } else if ("getDate".equals(methodName)) {
            methodName = "getLong";
        } else if ("getInteger".equals(methodName)) {
            methodName = "getInt";
        }
        return methodName;
    }

}
