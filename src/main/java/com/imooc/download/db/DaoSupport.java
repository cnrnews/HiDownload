package com.imooc.download.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class DaoSupport<T> implements IDaoSupport<T> {

    private SQLiteDatabase mSqLiteDatabase;
    private Class<T> mClazz;

    private static final Object[] mPutMethodArgs = new Object[2];

    private static final Map<String, Method> mPutMethods
            = new ArrayMap();

    private QuerySupport<T> mQuerySupport = null;

    @Override
    public void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz) {
        this.mSqLiteDatabase = sqLiteDatabase;
        this.mClazz = clazz;

        StringBuffer sb = new StringBuffer();
        sb.append("create table if not exists ")
                .append(DaoUtil.getTableName(mClazz))
                .append("(id integer primary key autoincrement, ");

        // 获取Person中所有属性，有多少就可以获取多少

        // name就代表 Person中所有的 值，比如是 name、age等等；
        // type就代表的是该值对应的所有类型，比如是String、int等等；

        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // 获取的name
            String name = field.getName();
            //获取的name的类型type（其实就是text类型，在数据库中 text类型就对应的是String）
            String type = field.getType().getSimpleName();

            // 类型转换 int -> integer String -> text
            sb.append(name).append(DaoUtil.getColumnType(type)).append(", ");
        }

        // 这里是把最后的 ", " 替换成 ")"
        sb.replace(sb.length()-2,sb.length(),")");
        Log.e("TAG","表语句-->"+sb.toString());

        // 执行建表语句
        mSqLiteDatabase.execSQL(sb.toString());

    }

    @Override
    public long insert(T obj) {
        //这里使用的其实还是原生的方式，只是把 obj转成ContentValues
        ContentValues values = contentValuesByObj(obj);
        return mSqLiteDatabase.insert(DaoUtil.getTableName(mClazz),null,values);
    }
    /**
     * 批量插入
     */
    @Override
    public void insert(List<T> datas) {
        // 批量插入采用事务优化
        mSqLiteDatabase.beginTransaction();
        for (T data : datas) {
            insert(data);
        }
        mSqLiteDatabase.setTransactionSuccessful();
        mSqLiteDatabase.endTransaction();
    }


    @Override
    public QuerySupport<T> querySupport() {
        if (mQuerySupport == null){
            mQuerySupport = new QuerySupport<>(mSqLiteDatabase,mClazz);
        }
        return mQuerySupport;
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

    /**
     *  把 obj 转成 ContentValues 利用反射
     *  反射其实就是获取的是属性、方法等东西
     */
    private ContentValues contentValuesByObj(T obj) {
        ContentValues values = new ContentValues();
        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            try{
                field.setAccessible(true);
                // 这里的key 就指的是获取的是 Person中的  name、age等所有字段，通过for循环有多少拿多少
                String name = field.getName();
//                获取value
                Object value = field.get(obj);

                mPutMethodArgs[0] = name;
                mPutMethodArgs[1] = value;

                String fieldTypeName = field.getType().getName();
                Method putMethod = mPutMethods.get(fieldTypeName);
                if (putMethod==null){
                    // 获取put()方法
                    putMethod = ContentValues.class.getMethod("put",String.class,value.getClass());
                    mPutMethods.put(fieldTypeName,putMethod);
                }
                // 通过反射执行
                putMethod.invoke(values,mPutMethodArgs);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                mPutMethodArgs[0] = null;
                mPutMethodArgs[1] = null;
            }
        }
        return values;
    }


    /**
     * 删除
     */
    public int delete(String whereClause, String[] whereArgs) {
        return mSqLiteDatabase.delete(DaoUtil.getTableName(mClazz), whereClause, whereArgs);
    }

    /**
     * 更新  这些你需要对  最原始的写法比较明了 extends
     */
    public int update(T obj, String whereClause, String... whereArgs) {
        ContentValues values = contentValuesByObj(obj);
        return mSqLiteDatabase.update(DaoUtil.getTableName(mClazz),
                values, whereClause, whereArgs);
    }

}
