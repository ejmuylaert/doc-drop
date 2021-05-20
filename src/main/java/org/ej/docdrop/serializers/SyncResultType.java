package org.ej.docdrop.serializers;

import org.ej.docdrop.domain.SyncResult;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class SyncResultType extends EnumType<SyncResult> {
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        st.setObject(
                index,
                value != null ? ((SyncResult) value).name() : null,
                Types.OTHER
        );
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String string = rs.getString(names[0]);

        if (string != null) {
            return SyncResult.valueOf(string);
        }

        return null;
    }
}
