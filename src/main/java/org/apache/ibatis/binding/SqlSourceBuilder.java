package org.apache.ibatis.binding;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotation.Column;
import org.apache.ibatis.annotation.Id;
import org.apache.ibatis.annotation.Model;
import org.apache.ibatis.annotation.TableModel;
import org.apache.ibatis.function.*;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlSourceBuilder {

    public static final String LIST = "list", ITEM = "i", SEPARATOR = ",", NULL = null, IDS = "ids",
            OPEN = "(", CLOSE = ")", INSERT = "insert", REPLACE = "replace";

    private final Configuration configuration;

    private final Class<?> mapper;

    private final Method method;

    private final String name;

    public SqlSourceBuilder(Configuration configuration, Class<?> mapper, Method method) {
        this.configuration = configuration;
        this.mapper = mapper;
        this.method = method;
        this.name = mapper.getName() + "." + method.getName();
    }

    public void addMappedStatement(SqlCommandType type) {
        synchronized (method) { // 避免 多线程 线程不安全问题
            TableModel tableModel = mapper.getAnnotation(TableModel.class);
            if (tableModel == null) {
                throw new BindingException(String.format("class %s should be marked with TableModel", mapper));
            }
            Class<?> declaringClass = method.getDeclaringClass();
            Class<?> model = tableModel.value();
            Model modelAnnotation = model.getAnnotation(Model.class);
            if (modelAnnotation == null) {
                throw new BindingException(String.format("model class %s should be marked with Model", model));
            }
            String table = modelAnnotation.table();
            if (StringUtils.isBlank(table)) {
                throw new BindingException("table should not be blank");
            }
            List<Field> fields = this.getFields(model);
            if (declaringClass.equals(Save.class)) {
                String sql = this.sqlInsert(table, INSERT, fields);
                SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(Merge.class)) {
                String sql = this.sqlInsert(table, REPLACE, fields);
                SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(BatchSave.class)) {
                SqlSource sqlSource = this.sqlSourceBatchSave(table, fields);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(BatchMerge.class)) {
                SqlSource sqlSource = this.sqlSourceBatchMerge(table, fields);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(Update.class)) {
                String sql = this.sqlUpdate(table, fields);
                SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(DeleteById.class)) {
                String sql = this.sqlDelete(table, fields);
                SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(DeleteByIds.class)) {
                SqlSource sqlSource = this.sqlSourceDeleteByIds(table, fields);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(DeleteAll.class)) {
                String sql = this.sqlDeleteAll(table);
                SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
                this.addSource(type, sqlSource);
            } else if (declaringClass.equals(FindById.class)) {
                String sql = this.sqlFind(table, fields);
                SqlSource sqlSource = new RawSqlSource(configuration, sql, null);
                this.addSource(type, sqlSource, model, fields);
            } else if (declaringClass.equals(FindByIds.class)) {
                SqlSource sqlSource = this.sqlSourceFindByIds(table, fields);
                this.addSource(type, sqlSource, model, fields);
            } else {
                throw new BindingException(String.format("class %s not support yet", declaringClass));
            }
        }
    }

    private void addSource(SqlCommandType type, SqlSource sqlSource, Class<?> model, List<Field> fields) {
        List<ResultMap> resultMaps = this.toList(this.genResultMap(name, model, fields));
        MappedStatement ms = new MappedStatement.Builder(configuration, name, sqlSource, type)
                .resultMaps(resultMaps).build();
        if (configuration.hasStatement(ms.getId())) {
            return;
        }
        configuration.addMappedStatement(ms);
    }

    private void addSource(SqlCommandType type, SqlSource sqlSource) {
        MappedStatement ms = new MappedStatement.Builder(configuration, name, sqlSource, type).build();
        if (configuration.hasStatement(ms.getId())) {
            return;
        }
        configuration.addMappedStatement(ms);
    }

    private SqlSource sqlSourceBatchSave(String table, List<Field> fields) {
        String mainText = this.textInsert(table, "insert", fields);
        String subText = this.textList(fields);
        MixedSqlNode sqlNode = this.getMixedSqlNode(mainText, subText, LIST, NULL, NULL);
        return new DynamicSqlSource(configuration, sqlNode);
    }

    private SqlSource sqlSourceBatchMerge(String table, List<Field> fields) {
        String mainText = this.textInsert(table, "replace", fields);
        String subText = this.textList(fields);
        MixedSqlNode sqlNode = this.getMixedSqlNode(mainText, subText, LIST, NULL, NULL);
        return new DynamicSqlSource(configuration, sqlNode);
    }

    private SqlSource sqlSourceDeleteByIds(String table, List<Field> fields) {
        Field idField = this.getIdField(fields);
        String mainText = "delete from " + table + " where " + idField.getName() + " in";
        String subText = "#{i}";
        MixedSqlNode sqlNode = this.getMixedSqlNode(mainText, subText, IDS, OPEN, CLOSE);
        return new DynamicSqlSource(configuration, sqlNode);
    }

    private SqlSource sqlSourceFindByIds(String table, List<Field> fields) {
        Field idField = this.getIdField(fields);
        String mainText = this.textSelect(table, fields) + idField.getName() + " in";
        String subText = "#{i}";
        MixedSqlNode sqlNode = this.getMixedSqlNode(mainText, subText, IDS, OPEN, CLOSE);
        return new DynamicSqlSource(configuration, sqlNode);
    }

    private MixedSqlNode getMixedSqlNode(String mainText, String subText, String list, String open, String close) {
        TextSqlNode head = new TextSqlNode(mainText);
        TextSqlNode include = new TextSqlNode(subText);
        MixedSqlNode contents = new MixedSqlNode(this.toList(include));
        ForEachSqlNode node = new ForEachSqlNode(configuration, contents, list, NULL, ITEM, open, close, SEPARATOR);
        return new MixedSqlNode(Arrays.asList(head, node));
    }

    private String textList(List<Field> fields) {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            builder.append("#{i.").append(field.getName()).append("}");
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private String sqlFind(String table, List<Field> fields) {
        Field idField = this.getIdField(fields);
        return this.textSelect(table, fields) + this.getColumn(idField) + " = " + "#{" + idField.getName() + "}";
    }

    private String sqlDelete(String table, List<Field> fields) {
        Field idField = this.getIdField(fields);
        return "delete from " + table + " where " + this.getColumn(idField) + " = " + "#{" + idField.getName() + "}";
    }

    private String sqlDeleteAll(String table) {
        return "delete from " + table;
    }

    private String textInsert(String table, String type, List<Field> fields) {
        StringBuilder builder = new StringBuilder(type).append(" into ").append(table).append("(");
        this.appendColumns(fields, builder);
        builder.append(") values");
        return builder.toString();
    }

    private String textSelect(String table, List<Field> fields) {
        StringBuilder builder = new StringBuilder("select ");
        this.appendColumns(fields, builder);
        builder.append(" from ").append(table).append(" where ");
        return builder.toString();
    }

    private Field getIdField(List<Field> fields) {
        Field idField = null;
        for (Field field : fields) {
            Id annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                idField = field;
                break;
            }
        }
        if (idField == null) {
            throw new BindingException("there is no field marked as Id");
        }
        return idField;
    }

    private String sqlUpdate(String table, List<Field> fields) {
        Field idField = this.getIdField(fields);
        fields.remove(idField);
        StringBuilder builder = new StringBuilder("update ").append(table).append(" set ");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            builder.append(this.getColumn(field)).append(" = ");
            builder.append("#{").append(field.getName()).append("}");
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(" where ").append(this.getColumn(idField)).append(" = ")
                .append("#{").append(idField.getName()).append("}");
        return builder.toString();
    }

    private String sqlInsert(String table, String type, List<Field> fields) {
        StringBuilder builder = new StringBuilder(this.textInsert(table, type, fields)).append("(");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            builder.append("#{").append(field.getName()).append("}");
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private void appendColumns(List<Field> fields, StringBuilder builder) {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            builder.append(this.getColumn(field));
            if (i < fields.size() - 1) {
                builder.append(", ");
            }
        }
    }

    private ResultMap genResultMap(String id, Class<?> type, List<Field> fields) {
        List<ResultMapping> resultMappings = new ArrayList<>();
        for (Field field : fields) {
            ResultMapping mapping = new ResultMapping
                    .Builder(configuration, field.getName(), this.getColumn(field), field.getType()).build();
            resultMappings.add(mapping);
        }
        return new ResultMap.Builder(configuration, id + ".ResultMap", type, resultMappings).build();
    }

    private List<Field> getFields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        Model ann = cls.getAnnotation(Model.class);
        Set<String> excludeFields = Arrays.stream(ann.excludeFields()).collect(Collectors.toSet());
        this.doAddFields(cls, fields, excludeFields);
        return fields;
    }

    private void doAddFields(Class<?> cls, List<Field> fields, Set<String> excludeFields) {
        if (cls != Object.class) {
            this.doAddFields(cls.getSuperclass(), fields, excludeFields);
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                String name = field.getName();
                if (!excludeFields.contains(name)) {
                    fields.add(field);
                }
            }
        }
    }

    private String getColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            return column.value();
        }
        String name = field.getName();
        return name.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    @SafeVarargs
    private final <T> List<T> toList(T... ts) {
        return Arrays.asList(ts);
    }

}
