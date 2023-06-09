package com.fpmislata.grup4pawparadise.persistence.impl;

import com.fpmislata.grup4pawparadise.business.entity.Category;
import com.fpmislata.grup4pawparadise.database.JDBCUtil;
import com.fpmislata.grup4pawparadise.exception.ResourceNotFoundException;
import com.fpmislata.grup4pawparadise.persistence.CategoryRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCCategoryRepository implements CategoryRepository {

    private static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found with id: ";
    private static final String SQL_EXCEPTION_MESSAGE = "SQL error occurred: ";

    @Override
    public List<Category> getAll(String language) {
        final String SQL = "SELECT c.*, cl.name_category FROM category c JOIN category_language cl ON c.id_category" +
                " = cl.id_category WHERE cl.language_type = ? ORDER BY c.id_category";
        List<Object> params = List.of(language);
        List<Category> categories = new ArrayList<>();
        Map<Integer, Category> categoryMap = new HashMap<>();

        Connection connection = JDBCUtil.open();
        ResultSet resultSet = JDBCUtil.select(connection, SQL, params);

        try {
            while (resultSet.next()) {
                Category category = createCategoryFromResultSet(resultSet);
                categoryMap.put(category.getId(), category);
                if (resultSet.getObject("id_parent") == null) {
                    categories.add(category);
                }
            }
            resultSet.beforeFirst();
            while (resultSet.next()) {
                if (resultSet.getObject("id_parent") != null) {
                    Category category = categoryMap.get(resultSet.getInt("id_category"));
                    Category parentCategory = categoryMap.get(resultSet.getInt("id_parent"));
                    if (parentCategory.getCategories() == null) {
                        parentCategory.setCategories(new ArrayList<>());
                    }
                    parentCategory.getCategories().add(category);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(SQL_EXCEPTION_MESSAGE + e.getMessage(), e);
        } finally {
            JDBCUtil.close(connection);
        }
        return categories;
    }

    @Override
    public List<Category> getChildrenByParentId(Integer parentId, String language) {
        String sql = "SELECT c.*, cl.name_category FROM category c JOIN category_language cl ON c.id_category = " +
                "cl.id_category WHERE c.id_parent IS NULL AND cl.language_type = ? ORDER BY c.id_category";

        if (parentId == null) {
            List<Object> params = List.of(language);
            return executeQuery(sql, params);
        }

        sql = "SELECT c.*, cl.name_category FROM category c JOIN category_language cl ON c.id_category = cl.id_category" +
                " WHERE c.id_parent = ? AND cl.language_type = ? ORDER BY c.id_category";
        List<Object> params = List.of(parentId, language);

        return executeQuery(sql, params);
    }

    private List<Category> executeQuery(String query, List<Object> params) {
        List<Category> categories = new ArrayList<>();

        Connection connection = JDBCUtil.open();
        ResultSet resultSet = JDBCUtil.select(connection, query, params);

        try {
            while (resultSet.next()) {
                categories.add(createCategoryFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(SQL_EXCEPTION_MESSAGE + e.getMessage(), e);
        } finally {
            JDBCUtil.close(connection);
        }

        return categories;
    }

    private Category createCategoryFromResultSet(ResultSet resultSet) throws SQLException {
        return new Category(resultSet.getInt("id_category"), resultSet.getString("name_category"),
                resultSet.getString("img_category"));
    }

    @Override
    public List<Category> getSuccessorsByParentId(Integer parentId, String language) {
        List<Category> successors = new ArrayList<>();
        addSuccessorsRecursively(parentId, language, successors);
        return successors;
    }

    private void addSuccessorsRecursively(Integer parentId, String language, List<Category> successors) {
        List<Category> children = getChildrenByParentId(parentId, language);

        for (Category child : children) {
            successors.add(child);
            addSuccessorsRecursively(child.getId(), language, successors);
        }
    }

    @Override
    public Category getById(int id, String language) throws ResourceNotFoundException {
        final String SQL = "SELECT c.*, cl.name_category FROM category c JOIN category_language cl ON c.id_category" +
                " = cl.id_category WHERE c.id_category = ? AND cl.language_type = ?";
        List<Object> params = List.of(id, language);

        Connection connection = JDBCUtil.open();
        ResultSet resultSet = JDBCUtil.select(connection, SQL, params);

        try {
            if (resultSet.next()) {
                return createCategoryFromResultSet(resultSet);
            } else {
                throw new ResourceNotFoundException(CATEGORY_NOT_FOUND_MESSAGE + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(SQL_EXCEPTION_MESSAGE + e.getMessage(), e);
        } finally {
            JDBCUtil.close(connection);
        }
    }
}
