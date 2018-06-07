dataSource {
    pooled = true
    jmxExport = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
//    cache.region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory' // Hibernate 3
    cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // Hibernate 4
    singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            pooled = false
            driverClassName = "com.mysql.jdbc.Driver"
            username = "root"
            password = "root"
            url = "jdbc:mysql://127.0.0.1:3306/identifyMe"
        }
    }
    test {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            URI jdbUri = new URI(System.getenv("JAWSDB_URL"))
            driverClassName = "com.mysql.jdbc.Driver"
            username = jdbUri.getUserInfo().split(":")[0]
            password = jdbUri.getUserInfo().split(":")[1]
            port = String.valueOf(jdbUri.getPort())
            url = "jdbc:mysql://" + jdbUri.getHost() + ":" + port + jdbUri.getPath()

            properties {
                testOnBorrow = false // en true puede tener impacto en la performance
                testOnReturn = false // en true puede tener impacto en la performance
                testWhileIdle = true

                validationQuery = 'SELECT 1 FROM DUAL'
                validationQueryTimeout = 3 //seg

                timeBetweenEvictionRunsMillis = 1000 * 30 //30 seg
                numTestsPerEvictionRun = 15 // valor superior al promedio de conexiones configuradas en el pool
                minEvictableIdleTimeMillis = 1000 * 15 // 15 seg

                removeAbandoned = true
                removeAbandonedTimeout = 60 * 15 //15 min

                maxWait = 100 //ms

                maxActive = 10
                maxIdle = 5
                minIdle = 3
                initialSize = 3
            }
        }
    }
}