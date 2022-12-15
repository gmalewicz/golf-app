package com.greg.golf.db;

import com.vladmihalcea.hibernate.type.ImmutableType;
import com.vladmihalcea.hibernate.type.array.*;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLHStoreType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLInetType;
import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import com.vladmihalcea.hibernate.type.interval.PostgreSQLPeriodType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.range.PostgreSQLRangeType;
import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.usertype.UserType;




@Slf4j
public class GolfPostgreSQLDialect extends PostgreSQLDialect {

    public GolfPostgreSQLDialect() {
        log.info("Using our own Postgres dialect.");
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);

        // Copy of the class hiberate-types-52 HibernateTypesContributor, as its kind of old and doesn't add the types
        // below when not extending from the deprecated old PostgreSQLDialect file.
        contributeType(typeContributions, BooleanArrayType.INSTANCE);
        contributeType(typeContributions, DateArrayType.INSTANCE);
        contributeType(typeContributions, DecimalArrayType.INSTANCE);
        contributeType(typeContributions, DoubleArrayType.INSTANCE);
        contributeType(typeContributions, EnumArrayType.INSTANCE);
        contributeType(typeContributions, IntArrayType.INSTANCE);
        contributeType(typeContributions, DoubleArrayType.INSTANCE);
        contributeType(typeContributions, ListArrayType.INSTANCE);
        contributeType(typeContributions, LocalDateArrayType.INSTANCE);
        contributeType(typeContributions, LocalDateTimeArrayType.INSTANCE);
        contributeType(typeContributions, LongArrayType.INSTANCE);
        contributeType(typeContributions, StringArrayType.INSTANCE);
        contributeType(typeContributions, TimestampArrayType.INSTANCE);
        contributeType(typeContributions, UUIDArrayType.INSTANCE);
        contributeType(typeContributions, PostgreSQLIntervalType.INSTANCE);
        contributeType(typeContributions, PostgreSQLPeriodType.INSTANCE);
        contributeType(typeContributions, JsonBinaryType.INSTANCE);
        contributeType(typeContributions, PostgreSQLTSVectorType.INSTANCE);
        contributeType(typeContributions, PostgreSQLEnumType.INSTANCE);
        contributeType(typeContributions, PostgreSQLHStoreType.INSTANCE);
        contributeType(typeContributions, PostgreSQLInetType.INSTANCE);
        contributeType(typeContributions, PostgreSQLRangeType.INSTANCE);
    }

    private PostgreSQLDialect contributeType(TypeContributions typeContributions, UserType type) {

        typeContributions.contributeType(type);

        return this;
    }
}
