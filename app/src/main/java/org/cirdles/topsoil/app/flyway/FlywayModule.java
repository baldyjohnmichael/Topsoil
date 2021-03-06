/*
 * Copyright 2015 CIRDLES.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.topsoil.app.flyway;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

/**
 * Created by johnzeringue on 8/28/15.
 */
public class FlywayModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FlywayMigrateTask.class);
    }

    @Provides
    Flyway provideFlyway(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        return flyway;
    }

}
