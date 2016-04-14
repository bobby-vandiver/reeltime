databaseChangeLog = {

	changeSet(author: "quartz.generated", id: "add-quartz-tables") {

		createTable(tableName: "QRTZ_CALENDARS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "CALENDAR_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "CALENDAR", type: "BLOB") {
				constraints(nullable: "false")
			}
		}

		createTable(tableName: "QRTZ_CRON_TRIGGERS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "CRON_EXPRESSION", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "TIME_ZONE_ID", type: "VARCHAR(80)")
		}

		createTable(tableName: "QRTZ_FIRED_TRIGGERS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ENTRY_ID", type: "VARCHAR(95)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "INSTANCE_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "FIRED_TIME", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "SCHED_TIME", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "PRIORITY", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "STATE", type: "VARCHAR(16)") {
				constraints(nullable: "false")
			}

			column(name: "JOB_NAME", type: "VARCHAR(200)")

			column(name: "JOB_GROUP", type: "VARCHAR(200)")

			column(name: "IS_NONCONCURRENT", type: "BOOLEAN")

			column(name: "REQUESTS_RECOVERY", type: "BOOLEAN")
		}

		createTable(tableName: "QRTZ_PAUSED_TRIGGER_GRPS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}
		}

		createTable(tableName: "QRTZ_SCHEDULER_STATE") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "INSTANCE_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "LAST_CHECKIN_TIME", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "CHECKIN_INTERVAL", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}

		createTable(tableName: "QRTZ_LOCKS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "LOCK_NAME", type: "VARCHAR(40)") {
				constraints(nullable: "false", primaryKey: "true")
			}
		}

		createTable(tableName: "QRTZ_JOB_DETAILS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "JOB_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "JOB_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "DESCRIPTION", type: "VARCHAR(250)")

			column(name: "JOB_CLASS_NAME", type: "VARCHAR(250)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DURABLE", type: "BOOLEAN") {
				constraints(nullable: "false")
			}

			column(name: "IS_NONCONCURRENT", type: "BOOLEAN") {
				constraints(nullable: "false")
			}

			column(name: "IS_UPDATE_DATA", type: "BOOLEAN") {
				constraints(nullable: "false")
			}

			column(name: "REQUESTS_RECOVERY", type: "BOOLEAN") {
				constraints(nullable: "false")
			}

			column(name: "JOB_DATA", type: "BLOB")
		}

		createTable(tableName: "QRTZ_SIMPLE_TRIGGERS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "REPEAT_COUNT", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "REPEAT_INTERVAL", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "TIMES_TRIGGERED", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}

		createTable(tableName: "QRTZ_SIMPROP_TRIGGERS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "STR_PROP_1", type: "VARCHAR(512)")

			column(name: "STR_PROP_2", type: "VARCHAR(512)")

			column(name: "STR_PROP_3", type: "VARCHAR(512)")

			column(name: "INT_PROP_1", type: "INT")

			column(name: "INT_PROP_2", type: "INT")

			column(name: "LONG_PROP_1", type: "BIGINT")

			column(name: "LONG_PROP_2", type: "BIGINT")

			column(name: "DEC_PROP_1", type: "NUMERIC(13,4)")

			column(name: "DEC_PROP_2", type: "NUMERIC(13,4)")

			column(name: "BOOL_PROP_1", type: "BOOLEAN")

			column(name: "BOOL_PROP_2", type: "BOOLEAN")
		}

		createTable(tableName: "QRTZ_BLOB_TRIGGERS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "JOB_DATA", type: "BLOB")
		}

		createTable(tableName: "QRTZ_TRIGGERS") {
			column(name: "SCHED_NAME", type: "VARCHAR(120)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "JOB_NAME", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "JOB_GROUP", type: "VARCHAR(200)") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "VARCHAR(250)")

			column(name: "NEXT_FIRE_TIME", type: "BIGINT")

			column(name: "PREV_FIRE_TIME", type: "BIGINT")

			column(name: "PRIORITY", type: "INT")

			column(name: "TRIGGER_STATE", type: "VARCHAR(16)") {
				constraints(nullable: "false")
			}

			column(name: "TRIGGER_TYPE", type: "VARCHAR(8)") {
				constraints(nullable: "false")
			}

			column(name: "START_TIME", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "END_TIME", type: "BIGINT")

			column(name: "CALENDAR_NAME", type: "VARCHAR(200)")

			column(name: "MISFIRE_INSTR", type: "SMALLINT")

			column(name: "JOB_DATA", type: "BLOB")
		}
	}

	changeSet(author: "quartz.generated", id: "add-quartz-foreign-keys") {
		addForeignKeyConstraint(
                baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP",
                baseTableName: "QRTZ_CRON_TRIGGERS",
                constraintName: "FK_QRTZ_CRON_TRIGGERS_QRTZ_TRIGGERS",
                deferrable: "false",
                initiallyDeferred: "false",
                onDelete: "CASCADE",
                onUpdate: "NO ACTION",
                referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP",
                referencedTableName: "QRTZ_TRIGGERS",
                referencesUniqueColumn: "false"
        )

		addForeignKeyConstraint(
                baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP",
                baseTableName: "QRTZ_SIMPLE_TRIGGERS",
                constraintName: "FK_QRTZ_SIMPLE_TRIGGERS_QRTZ_TRIGGERS",
                deferrable: "false",
                initiallyDeferred: "false",
                onDelete: "CASCADE",
                onUpdate: "NO ACTION",
                referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP",
                referencedTableName: "QRTZ_TRIGGERS",
                referencesUniqueColumn: "false"
        )

		addForeignKeyConstraint(
                baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP",
                baseTableName: "QRTZ_SIMPROP_TRIGGERS",
                constraintName: "FK_QRTZ_SIMPROP_TRIGGERS_QRTZ_TRIGGERS",
                deferrable: "false",
                initiallyDeferred: "false",
                onDelete: "CASCADE",
                onUpdate: "NO ACTION",
                referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP",
                referencedTableName: "QRTZ_TRIGGERS",
                referencesUniqueColumn: "false"
        )

		addForeignKeyConstraint(
                baseColumnNames: "SCHED_NAME, JOB_NAME, JOB_GROUP",
                baseTableName: "QRTZ_TRIGGERS",
                constraintName: "FK_QRTZ_TRIGGERS_QRTZ_JOB_DETAILS",
                deferrable: "false",
                initiallyDeferred: "false",
                onDelete: "CASCADE",
                onUpdate: "NO ACTION",
                referencedColumnNames: "SCHED_NAME, JOB_NAME, JOB_GROUP",
                referencedTableName: "QRTZ_JOB_DETAILS",
                referencesUniqueColumn: "false"
        )
	}

	changeSet(author: "quartz.generated", id: "add-quartz-indices") {

        createIndex(indexName: "IDX_QRTZ_T_NEXT_FIRE_TIME", tableName: "QRTZ_TRIGGERS") {
            column(name: "NEXT_FIRE_TIME")
        }

        createIndex(indexName: "IDX_QRTZ_T_STATE", tableName: "QRTZ_TRIGGERS") {
            column(name: "TRIGGER_STATE")
        }

        createIndex(indexName: "IDX_QRTZ_T_NF_ST", tableName: "QRTZ_TRIGGERS") {
            column(name: "TRIGGER_STATE")

            column(name: "NEXT_FIRE_TIME")
        }

        createIndex(indexName: "IDX_QRTZ_FT_TRIG_NAME", tableName: "QRTZ_FIRED_TRIGGERS") {
            column(name: "TRIGGER_NAME")
        }

        createIndex(indexName: "IDX_QRTZ_FT_TRIG_GROUP", tableName: "QRTZ_FIRED_TRIGGERS") {
            column(name: "TRIGGER_GROUP")
        }

        createIndex(indexName: "IDX_QRTZ_FT_TRIG_N_G", tableName: "QRTZ_FIRED_TRIGGERS") {
            column(name: "TRIGGER_NAME")

            column(name: "TRIGGER_GROUP")
        }

        createIndex(indexName: "IDX_QRTZ_FT_TRIG_INST_NAME", tableName: "QRTZ_FIRED_TRIGGERS") {
            column(name: "INSTANCE_NAME")
        }

        createIndex(indexName: "IDX_QRTZ_FT_JOB_NAME", tableName: "QRTZ_FIRED_TRIGGERS") {
            column(name: "JOB_NAME")
        }

        createIndex(indexName: "IDX_QRTZ_FT_JOB_GROUP", tableName: "QRTZ_FIRED_TRIGGERS") {
            column(name: "JOB_GROUP")
        }

        createIndex(indexName: "IDX_QRTZ_T_NEXT_FIRE_TIME_MISFIRE", tableName: "QRTZ_TRIGGERS") {
            column(name: "MISFIRE_INSTR")

            column(name: "NEXT_FIRE_TIME")
        }

        createIndex(indexName: "IDX_QRTZ_T_NF_ST_MISFIRE", tableName: "QRTZ_TRIGGERS") {
            column(name: "MISFIRE_INSTR")

            column(name: "NEXT_FIRE_TIME")

            column(name: "TRIGGER_STATE")
        }

        createIndex(indexName: "IDX_QRTZ_T_NF_ST_MISFIRE_GRP", tableName: "QRTZ_TRIGGERS") {
            column(name: "MISFIRE_INSTR")

            column(name: "TRIGGER_GROUP")

            column(name: "TRIGGER_STATE")
        }
    }
}