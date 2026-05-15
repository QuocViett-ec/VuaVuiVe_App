package vn.vuavuive.shared.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CartDao_Impl implements CartDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CartItemEntity> __insertionAdapterOfCartItemEntity;

  private final EntityDeletionOrUpdateAdapter<CartItemEntity> __deletionAdapterOfCartItemEntity;

  private final EntityDeletionOrUpdateAdapter<CartItemEntity> __updateAdapterOfCartItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfClearCart;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final SharedSQLiteStatement __preparedStmtOfUpdateQuantity;

  private final SharedSQLiteStatement __preparedStmtOfSetSavedForLater;

  public CartDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCartItemEntity = new EntityInsertionAdapter<CartItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cart_items` (`productId`,`quantity`,`productName`,`productPrice`,`productImageUrl`,`productUnit`,`productStock`,`addedAt`,`savedForLater`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final CartItemEntity entity) {
        if (entity.getProductId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getProductId());
        }
        statement.bindLong(2, entity.getQuantity());
        if (entity.getProductName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getProductName());
        }
        statement.bindDouble(4, entity.getProductPrice());
        if (entity.getProductImageUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getProductImageUrl());
        }
        if (entity.getProductUnit() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getProductUnit());
        }
        statement.bindLong(7, entity.getProductStock());
        statement.bindLong(8, entity.getAddedAt());
        final int _tmp = entity.isSavedForLater() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__deletionAdapterOfCartItemEntity = new EntityDeletionOrUpdateAdapter<CartItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `cart_items` WHERE `productId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final CartItemEntity entity) {
        if (entity.getProductId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getProductId());
        }
      }
    };
    this.__updateAdapterOfCartItemEntity = new EntityDeletionOrUpdateAdapter<CartItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `cart_items` SET `productId` = ?,`quantity` = ?,`productName` = ?,`productPrice` = ?,`productImageUrl` = ?,`productUnit` = ?,`productStock` = ?,`addedAt` = ?,`savedForLater` = ? WHERE `productId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final CartItemEntity entity) {
        if (entity.getProductId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getProductId());
        }
        statement.bindLong(2, entity.getQuantity());
        if (entity.getProductName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getProductName());
        }
        statement.bindDouble(4, entity.getProductPrice());
        if (entity.getProductImageUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getProductImageUrl());
        }
        if (entity.getProductUnit() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getProductUnit());
        }
        statement.bindLong(7, entity.getProductStock());
        statement.bindLong(8, entity.getAddedAt());
        final int _tmp = entity.isSavedForLater() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getProductId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getProductId());
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cart_items WHERE productId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearCart = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cart_items WHERE savedForLater = 0";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cart_items";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateQuantity = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE cart_items SET quantity = ? WHERE productId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetSavedForLater = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE cart_items SET savedForLater = ? WHERE productId = ?";
        return _query;
      }
    };
  }

  @Override
  public void upsert(final CartItemEntity item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfCartItemEntity.insert(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertOrUpdate(final CartItemEntity item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfCartItemEntity.insert(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<CartItemEntity> items) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfCartItemEntity.insert(items);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final CartItemEntity item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfCartItemEntity.handle(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final CartItemEntity item) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfCartItemEntity.handle(item);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final String productId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
    int _argIndex = 1;
    if (productId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, productId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDelete.release(_stmt);
    }
  }

  @Override
  public void deleteById(final String productId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
    int _argIndex = 1;
    if (productId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, productId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDelete.release(_stmt);
    }
  }

  @Override
  public void clearCart() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearCart.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearCart.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public void clearAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public void updateQuantity(final String productId, final int quantity) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateQuantity.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, quantity);
    _argIndex = 2;
    if (productId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, productId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateQuantity.release(_stmt);
    }
  }

  @Override
  public void setSavedForLater(final String productId, final boolean savedForLater) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfSetSavedForLater.acquire();
    int _argIndex = 1;
    final int _tmp = savedForLater ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    if (productId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, productId);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfSetSavedForLater.release(_stmt);
    }
  }

  @Override
  public LiveData<List<CartItemEntity>> getCartItems() {
    final String _sql = "SELECT * FROM cart_items WHERE savedForLater = 0 ORDER BY addedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"cart_items"}, false, new Callable<List<CartItemEntity>>() {
      @Override
      @Nullable
      public List<CartItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "productPrice");
          final int _cursorIndexOfProductImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "productImageUrl");
          final int _cursorIndexOfProductUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "productUnit");
          final int _cursorIndexOfProductStock = CursorUtil.getColumnIndexOrThrow(_cursor, "productStock");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfSavedForLater = CursorUtil.getColumnIndexOrThrow(_cursor, "savedForLater");
          final List<CartItemEntity> _result = new ArrayList<CartItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CartItemEntity _item;
            _item = new CartItemEntity();
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            _item.setProductId(_tmpProductId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            _item.setQuantity(_tmpQuantity);
            final String _tmpProductName;
            if (_cursor.isNull(_cursorIndexOfProductName)) {
              _tmpProductName = null;
            } else {
              _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            }
            _item.setProductName(_tmpProductName);
            final double _tmpProductPrice;
            _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            _item.setProductPrice(_tmpProductPrice);
            final String _tmpProductImageUrl;
            if (_cursor.isNull(_cursorIndexOfProductImageUrl)) {
              _tmpProductImageUrl = null;
            } else {
              _tmpProductImageUrl = _cursor.getString(_cursorIndexOfProductImageUrl);
            }
            _item.setProductImageUrl(_tmpProductImageUrl);
            final String _tmpProductUnit;
            if (_cursor.isNull(_cursorIndexOfProductUnit)) {
              _tmpProductUnit = null;
            } else {
              _tmpProductUnit = _cursor.getString(_cursorIndexOfProductUnit);
            }
            _item.setProductUnit(_tmpProductUnit);
            final int _tmpProductStock;
            _tmpProductStock = _cursor.getInt(_cursorIndexOfProductStock);
            _item.setProductStock(_tmpProductStock);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item.setAddedAt(_tmpAddedAt);
            final boolean _tmpSavedForLater;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSavedForLater);
            _tmpSavedForLater = _tmp != 0;
            _item.setSavedForLater(_tmpSavedForLater);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CartItemEntity>> getSavedItems() {
    final String _sql = "SELECT * FROM cart_items WHERE savedForLater = 1 ORDER BY addedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"cart_items"}, false, new Callable<List<CartItemEntity>>() {
      @Override
      @Nullable
      public List<CartItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "productPrice");
          final int _cursorIndexOfProductImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "productImageUrl");
          final int _cursorIndexOfProductUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "productUnit");
          final int _cursorIndexOfProductStock = CursorUtil.getColumnIndexOrThrow(_cursor, "productStock");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfSavedForLater = CursorUtil.getColumnIndexOrThrow(_cursor, "savedForLater");
          final List<CartItemEntity> _result = new ArrayList<CartItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CartItemEntity _item;
            _item = new CartItemEntity();
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            _item.setProductId(_tmpProductId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            _item.setQuantity(_tmpQuantity);
            final String _tmpProductName;
            if (_cursor.isNull(_cursorIndexOfProductName)) {
              _tmpProductName = null;
            } else {
              _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            }
            _item.setProductName(_tmpProductName);
            final double _tmpProductPrice;
            _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            _item.setProductPrice(_tmpProductPrice);
            final String _tmpProductImageUrl;
            if (_cursor.isNull(_cursorIndexOfProductImageUrl)) {
              _tmpProductImageUrl = null;
            } else {
              _tmpProductImageUrl = _cursor.getString(_cursorIndexOfProductImageUrl);
            }
            _item.setProductImageUrl(_tmpProductImageUrl);
            final String _tmpProductUnit;
            if (_cursor.isNull(_cursorIndexOfProductUnit)) {
              _tmpProductUnit = null;
            } else {
              _tmpProductUnit = _cursor.getString(_cursorIndexOfProductUnit);
            }
            _item.setProductUnit(_tmpProductUnit);
            final int _tmpProductStock;
            _tmpProductStock = _cursor.getInt(_cursorIndexOfProductStock);
            _item.setProductStock(_tmpProductStock);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item.setAddedAt(_tmpAddedAt);
            final boolean _tmpSavedForLater;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSavedForLater);
            _tmpSavedForLater = _tmp != 0;
            _item.setSavedForLater(_tmpSavedForLater);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CartItemEntity>> getSavedForLaterItems() {
    final String _sql = "SELECT * FROM cart_items WHERE savedForLater = 1 ORDER BY addedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"cart_items"}, false, new Callable<List<CartItemEntity>>() {
      @Override
      @Nullable
      public List<CartItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "productPrice");
          final int _cursorIndexOfProductImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "productImageUrl");
          final int _cursorIndexOfProductUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "productUnit");
          final int _cursorIndexOfProductStock = CursorUtil.getColumnIndexOrThrow(_cursor, "productStock");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfSavedForLater = CursorUtil.getColumnIndexOrThrow(_cursor, "savedForLater");
          final List<CartItemEntity> _result = new ArrayList<CartItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CartItemEntity _item;
            _item = new CartItemEntity();
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            _item.setProductId(_tmpProductId);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            _item.setQuantity(_tmpQuantity);
            final String _tmpProductName;
            if (_cursor.isNull(_cursorIndexOfProductName)) {
              _tmpProductName = null;
            } else {
              _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            }
            _item.setProductName(_tmpProductName);
            final double _tmpProductPrice;
            _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            _item.setProductPrice(_tmpProductPrice);
            final String _tmpProductImageUrl;
            if (_cursor.isNull(_cursorIndexOfProductImageUrl)) {
              _tmpProductImageUrl = null;
            } else {
              _tmpProductImageUrl = _cursor.getString(_cursorIndexOfProductImageUrl);
            }
            _item.setProductImageUrl(_tmpProductImageUrl);
            final String _tmpProductUnit;
            if (_cursor.isNull(_cursorIndexOfProductUnit)) {
              _tmpProductUnit = null;
            } else {
              _tmpProductUnit = _cursor.getString(_cursorIndexOfProductUnit);
            }
            _item.setProductUnit(_tmpProductUnit);
            final int _tmpProductStock;
            _tmpProductStock = _cursor.getInt(_cursorIndexOfProductStock);
            _item.setProductStock(_tmpProductStock);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item.setAddedAt(_tmpAddedAt);
            final boolean _tmpSavedForLater;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSavedForLater);
            _tmpSavedForLater = _tmp != 0;
            _item.setSavedForLater(_tmpSavedForLater);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<CartItemEntity> getAllCartItemsSync() {
    final String _sql = "SELECT * FROM cart_items ORDER BY addedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
      final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
      final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "productPrice");
      final int _cursorIndexOfProductImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "productImageUrl");
      final int _cursorIndexOfProductUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "productUnit");
      final int _cursorIndexOfProductStock = CursorUtil.getColumnIndexOrThrow(_cursor, "productStock");
      final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
      final int _cursorIndexOfSavedForLater = CursorUtil.getColumnIndexOrThrow(_cursor, "savedForLater");
      final List<CartItemEntity> _result = new ArrayList<CartItemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final CartItemEntity _item;
        _item = new CartItemEntity();
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        _item.setProductId(_tmpProductId);
        final int _tmpQuantity;
        _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
        _item.setQuantity(_tmpQuantity);
        final String _tmpProductName;
        if (_cursor.isNull(_cursorIndexOfProductName)) {
          _tmpProductName = null;
        } else {
          _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
        }
        _item.setProductName(_tmpProductName);
        final double _tmpProductPrice;
        _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
        _item.setProductPrice(_tmpProductPrice);
        final String _tmpProductImageUrl;
        if (_cursor.isNull(_cursorIndexOfProductImageUrl)) {
          _tmpProductImageUrl = null;
        } else {
          _tmpProductImageUrl = _cursor.getString(_cursorIndexOfProductImageUrl);
        }
        _item.setProductImageUrl(_tmpProductImageUrl);
        final String _tmpProductUnit;
        if (_cursor.isNull(_cursorIndexOfProductUnit)) {
          _tmpProductUnit = null;
        } else {
          _tmpProductUnit = _cursor.getString(_cursorIndexOfProductUnit);
        }
        _item.setProductUnit(_tmpProductUnit);
        final int _tmpProductStock;
        _tmpProductStock = _cursor.getInt(_cursorIndexOfProductStock);
        _item.setProductStock(_tmpProductStock);
        final long _tmpAddedAt;
        _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
        _item.setAddedAt(_tmpAddedAt);
        final boolean _tmpSavedForLater;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfSavedForLater);
        _tmpSavedForLater = _tmp != 0;
        _item.setSavedForLater(_tmpSavedForLater);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<CartItemEntity> getAllSync() {
    final String _sql = "SELECT * FROM cart_items ORDER BY addedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
      final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
      final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "productPrice");
      final int _cursorIndexOfProductImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "productImageUrl");
      final int _cursorIndexOfProductUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "productUnit");
      final int _cursorIndexOfProductStock = CursorUtil.getColumnIndexOrThrow(_cursor, "productStock");
      final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
      final int _cursorIndexOfSavedForLater = CursorUtil.getColumnIndexOrThrow(_cursor, "savedForLater");
      final List<CartItemEntity> _result = new ArrayList<CartItemEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final CartItemEntity _item;
        _item = new CartItemEntity();
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        _item.setProductId(_tmpProductId);
        final int _tmpQuantity;
        _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
        _item.setQuantity(_tmpQuantity);
        final String _tmpProductName;
        if (_cursor.isNull(_cursorIndexOfProductName)) {
          _tmpProductName = null;
        } else {
          _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
        }
        _item.setProductName(_tmpProductName);
        final double _tmpProductPrice;
        _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
        _item.setProductPrice(_tmpProductPrice);
        final String _tmpProductImageUrl;
        if (_cursor.isNull(_cursorIndexOfProductImageUrl)) {
          _tmpProductImageUrl = null;
        } else {
          _tmpProductImageUrl = _cursor.getString(_cursorIndexOfProductImageUrl);
        }
        _item.setProductImageUrl(_tmpProductImageUrl);
        final String _tmpProductUnit;
        if (_cursor.isNull(_cursorIndexOfProductUnit)) {
          _tmpProductUnit = null;
        } else {
          _tmpProductUnit = _cursor.getString(_cursorIndexOfProductUnit);
        }
        _item.setProductUnit(_tmpProductUnit);
        final int _tmpProductStock;
        _tmpProductStock = _cursor.getInt(_cursorIndexOfProductStock);
        _item.setProductStock(_tmpProductStock);
        final long _tmpAddedAt;
        _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
        _item.setAddedAt(_tmpAddedAt);
        final boolean _tmpSavedForLater;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfSavedForLater);
        _tmpSavedForLater = _tmp != 0;
        _item.setSavedForLater(_tmpSavedForLater);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public CartItemEntity getCartItem(final String productId) {
    final String _sql = "SELECT * FROM cart_items WHERE productId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (productId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, productId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
      final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
      final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
      final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "productPrice");
      final int _cursorIndexOfProductImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "productImageUrl");
      final int _cursorIndexOfProductUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "productUnit");
      final int _cursorIndexOfProductStock = CursorUtil.getColumnIndexOrThrow(_cursor, "productStock");
      final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
      final int _cursorIndexOfSavedForLater = CursorUtil.getColumnIndexOrThrow(_cursor, "savedForLater");
      final CartItemEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new CartItemEntity();
        final String _tmpProductId;
        if (_cursor.isNull(_cursorIndexOfProductId)) {
          _tmpProductId = null;
        } else {
          _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
        }
        _result.setProductId(_tmpProductId);
        final int _tmpQuantity;
        _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
        _result.setQuantity(_tmpQuantity);
        final String _tmpProductName;
        if (_cursor.isNull(_cursorIndexOfProductName)) {
          _tmpProductName = null;
        } else {
          _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
        }
        _result.setProductName(_tmpProductName);
        final double _tmpProductPrice;
        _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
        _result.setProductPrice(_tmpProductPrice);
        final String _tmpProductImageUrl;
        if (_cursor.isNull(_cursorIndexOfProductImageUrl)) {
          _tmpProductImageUrl = null;
        } else {
          _tmpProductImageUrl = _cursor.getString(_cursorIndexOfProductImageUrl);
        }
        _result.setProductImageUrl(_tmpProductImageUrl);
        final String _tmpProductUnit;
        if (_cursor.isNull(_cursorIndexOfProductUnit)) {
          _tmpProductUnit = null;
        } else {
          _tmpProductUnit = _cursor.getString(_cursorIndexOfProductUnit);
        }
        _result.setProductUnit(_tmpProductUnit);
        final int _tmpProductStock;
        _tmpProductStock = _cursor.getInt(_cursorIndexOfProductStock);
        _result.setProductStock(_tmpProductStock);
        final long _tmpAddedAt;
        _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
        _result.setAddedAt(_tmpAddedAt);
        final boolean _tmpSavedForLater;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfSavedForLater);
        _tmpSavedForLater = _tmp != 0;
        _result.setSavedForLater(_tmpSavedForLater);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<Integer> getCartCount() {
    final String _sql = "SELECT COUNT(*) FROM cart_items WHERE savedForLater = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"cart_items"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Double> getCartTotal() {
    final String _sql = "SELECT SUM(productPrice * quantity) FROM cart_items WHERE savedForLater = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"cart_items"}, false, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
