databaseChangeLog = {

    changeSet(author: "bvandiver (generated)", id: "1460669746193-1") {
        createTable(tableName: "access_token") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "access_tokenPK")
            }

            column(name: "authentication", type: "BINARY(4096)") {
                constraints(nullable: "false")
            }

            column(name: "authentication_key", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "client_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "expiration", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "refresh_token", type: "VARCHAR(255)")

            column(name: "token_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR(255)")

            column(name: "value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-2") {
        createTable(tableName: "access_token_additional_information") {
            column(name: "additional_information", type: "BIGINT")

            column(name: "additional_information_idx", type: "VARCHAR(255)")

            column(name: "additional_information_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-3") {
        createTable(tableName: "access_token_scope") {
            column(name: "access_token_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "scope_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-4") {
        createTable(tableName: "account_code") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "account_codePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cost", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "salt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-5") {
        createTable(tableName: "audience_member") {
            column(name: "reel_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "member_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-6") {
        createTable(tableName: "authorization_code") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "authorization_codePK")
            }

            column(name: "authentication", type: "BINARY(4096)") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-7") {
        createTable(tableName: "client") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "clientPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "access_token_validity_seconds", type: "INT")

            column(name: "client_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "client_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "client_secret", type: "VARCHAR(255)")

            column(name: "refresh_token_validity_seconds", type: "INT")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-8") {
        createTable(tableName: "client_additional_information") {
            column(name: "additional_information", type: "BIGINT")

            column(name: "additional_information_idx", type: "VARCHAR(255)")

            column(name: "additional_information_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-9") {
        createTable(tableName: "client_authorities") {
            column(name: "client_id", type: "BIGINT")

            column(name: "authorities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-10") {
        createTable(tableName: "client_authorized_grant_types") {
            column(name: "client_id", type: "BIGINT")

            column(name: "authorized_grant_types_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-11") {
        createTable(tableName: "client_auto_approve_scopes") {
            column(name: "client_id", type: "BIGINT")

            column(name: "auto_approve_scopes_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-12") {
        createTable(tableName: "client_redirect_uris") {
            column(name: "client_id", type: "BIGINT")

            column(name: "redirect_uris_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-13") {
        createTable(tableName: "client_resource_ids") {
            column(name: "client_id", type: "BIGINT")

            column(name: "resource_ids_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-14") {
        createTable(tableName: "client_scopes") {
            column(name: "client_id", type: "BIGINT")

            column(name: "scopes_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-15") {
        createTable(tableName: "playlist") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "playlistPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "bandwidth", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "codecs", type: "VARCHAR(255)")

            column(name: "hls_version", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "media_sequence", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "program_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "resolution", type: "VARCHAR(255)")

            column(name: "target_duration", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-16") {
        createTable(tableName: "playlist_segment") {
            column(name: "playlist_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "segment_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-17") {
        createTable(tableName: "playlist_uri") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "playlist_uriPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-18") {
        createTable(tableName: "playlist_uri_video") {
            column(name: "playlist_uri_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "video_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-19") {
        createTable(tableName: "playlist_video") {
            column(name: "playlist_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "video_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-20") {
        createTable(tableName: "reel") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "reelPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(25)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-21") {
        createTable(tableName: "reel_video") {
            column(name: "reel_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "video_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-22") {
        createTable(tableName: "refresh_token") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "refresh_tokenPK")
            }

            column(name: "authentication", type: "BINARY(4096)") {
                constraints(nullable: "false")
            }

            column(name: "expiration", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-23") {
        createTable(tableName: "resource_removal_target") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "resource_removal_targetPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "base", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "relative", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-24") {
        createTable(tableName: "role") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "rolePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "authority", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-25") {
        createTable(tableName: "segment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "segmentPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "duration", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "segment_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-26") {
        createTable(tableName: "thumbnail") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "thumbnailPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "resolution", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-27") {
        createTable(tableName: "thumbnail_video") {
            column(name: "thumbnail_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "video_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-28") {
        createTable(tableName: "transcoder_job") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "transcoder_jobPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "job_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "video_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-29") {
        createTable(tableName: "user") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "userPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "account_expired", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "account_locked", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "display_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "enabled", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "password", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "password_expired", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "verified", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-30") {
        createTable(tableName: "user_activity") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "user_activityPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "reel_id", type: "BIGINT")

            column(name: "video_id", type: "BIGINT")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-31") {
        createTable(tableName: "user_client") {
            column(name: "user_clients_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "client_id", type: "BIGINT")
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-32") {
        createTable(tableName: "user_following") {
            column(name: "follower_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "followee_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-33") {
        createTable(tableName: "user_reel") {
            column(name: "owner_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "reel_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-34") {
        createTable(tableName: "user_role") {
            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "role_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-35") {
        createTable(tableName: "video") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "videoPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "available", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "master_path", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "master_thumbnail_path", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-36") {
        createTable(tableName: "video_creator") {
            column(name: "video_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "creator_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-37") {
        addPrimaryKey(columnNames: "reel_id, member_id", constraintName: "audience_memberPK", tableName: "audience_member")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-38") {
        addPrimaryKey(columnNames: "playlist_id, segment_id", constraintName: "playlist_segmentPK", tableName: "playlist_segment")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-39") {
        addPrimaryKey(columnNames: "playlist_uri_id, video_id", constraintName: "playlist_uri_videoPK", tableName: "playlist_uri_video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-40") {
        addPrimaryKey(columnNames: "playlist_id, video_id", constraintName: "playlist_videoPK", tableName: "playlist_video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-41") {
        addPrimaryKey(columnNames: "reel_id, video_id", constraintName: "reel_videoPK", tableName: "reel_video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-42") {
        addPrimaryKey(columnNames: "thumbnail_id, video_id", constraintName: "thumbnail_videoPK", tableName: "thumbnail_video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-43") {
        addPrimaryKey(columnNames: "follower_id, followee_id", constraintName: "user_followingPK", tableName: "user_following")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-44") {
        addPrimaryKey(columnNames: "owner_id, reel_id", constraintName: "user_reelPK", tableName: "user_reel")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-45") {
        addPrimaryKey(columnNames: "user_id, role_id", constraintName: "user_rolePK", tableName: "user_role")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-46") {
        addPrimaryKey(columnNames: "video_id, creator_id", constraintName: "video_creatorPK", tableName: "video_creator")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-47") {
        addUniqueConstraint(columnNames: "authentication_key", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "access_token")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-48") {
        addUniqueConstraint(columnNames: "value", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "access_token")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-49") {
        addUniqueConstraint(columnNames: "code", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "authorization_code")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-50") {
        addUniqueConstraint(columnNames: "client_id", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-51") {
        addUniqueConstraint(columnNames: "uri", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "playlist_uri")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-52") {
        addUniqueConstraint(columnNames: "value", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "refresh_token")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-53") {
        addUniqueConstraint(columnNames: "authority", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "role")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-54") {
        addUniqueConstraint(columnNames: "uri", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "segment")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-55") {
        addUniqueConstraint(columnNames: "uri", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "thumbnail")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-56") {
        addUniqueConstraint(columnNames: "video_id", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "transcoder_job")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-57") {
        addUniqueConstraint(columnNames: "username", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-58") {
        addUniqueConstraint(columnNames: "master_path", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-59") {
        addUniqueConstraint(columnNames: "master_thumbnail_path", deferrable: "false", disabled: "false", initiallyDeferred: "false", tableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-60") {
        addForeignKeyConstraint(baseColumnNames: "access_token_id", baseTableName: "access_token_scope", constraintName: "FK_1ciu6ihmgvnc43hsl4kftfx4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "access_token")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-61") {
        addForeignKeyConstraint(baseColumnNames: "follower_id", baseTableName: "user_following", constraintName: "FK_1eswg8xt3almkt2svjdig6e9f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-62") {
        addForeignKeyConstraint(baseColumnNames: "followee_id", baseTableName: "user_following", constraintName: "FK_1yxga191a7pc1gsipxv2ofd9m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-63") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "client_scopes", constraintName: "FK_2p2k8amjbv4nh49my1f1equ24", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-64") {
        addForeignKeyConstraint(baseColumnNames: "reel_id", baseTableName: "user_activity", constraintName: "FK_3dhxcwvvvwea7p12iwy35e20p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "reel")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-65") {
        addForeignKeyConstraint(baseColumnNames: "member_id", baseTableName: "audience_member", constraintName: "FK_3faetpuk190lm4af4blhtw2ye", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-66") {
        addForeignKeyConstraint(baseColumnNames: "reel_id", baseTableName: "user_reel", constraintName: "FK_3tk2xufql4i2ti2av3c9ec98u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "reel")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-67") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "playlist_video", constraintName: "FK_67qot9akjv7flb9hidtn8mih5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-68") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "client_resource_ids", constraintName: "FK_7a1dou4hws2q6l66r3beutr8f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-69") {
        addForeignKeyConstraint(baseColumnNames: "playlist_uri_id", baseTableName: "playlist_uri_video", constraintName: "FK_8oh1nxkigh8nmbh0tagvnsrxc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "playlist_uri")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-70") {
        addForeignKeyConstraint(baseColumnNames: "thumbnail_id", baseTableName: "thumbnail_video", constraintName: "FK_90hreamo11xbgsgc1m5ci89wn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "thumbnail")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-71") {
        addForeignKeyConstraint(baseColumnNames: "reel_id", baseTableName: "audience_member", constraintName: "FK_a47ld6q8dpm6h2lg1cbpv4h64", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "reel")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-72") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK_apcc8lxk2xnug8377fatvbn04", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-73") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "client_authorized_grant_types", constraintName: "FK_arha6vsat2mcvm83annbu7aug", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-74") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "user_reel", constraintName: "FK_ay8196brfoqepkrlpc8fl3p6f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-75") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "account_code", constraintName: "FK_b953xlpgybm9ss199c136e4n2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-76") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "transcoder_job", constraintName: "FK_cpe3y50jfm5mawip18csj4kji", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-77") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "reel_video", constraintName: "FK_exgvr62not6hioq8vxk5q2eco", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-78") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_activity", constraintName: "FK_h1k0x6ug1h1vv2y2og9exsms", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-79") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK_it77eq964jhfqtu54081ebtio", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-80") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "client_authorities", constraintName: "FK_j250lf10jjhc5n6qfn6nk1sn3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-81") {
        addForeignKeyConstraint(baseColumnNames: "playlist_id", baseTableName: "playlist_segment", constraintName: "FK_jibn1c670om4k7pujt05o597k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "playlist")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-82") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "playlist_uri_video", constraintName: "FK_kb208p14uqoqgeeuuq070av3x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-83") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "video_creator", constraintName: "FK_kqiv047ljp4yeoow13ejja7k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-84") {
        addForeignKeyConstraint(baseColumnNames: "playlist_id", baseTableName: "playlist_video", constraintName: "FK_lipyxgt2vm7x9w9v7s0qscvia", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "playlist")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-85") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "thumbnail_video", constraintName: "FK_ljna4y70jiqi2c9fyi7ef7n8o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-86") {
        addForeignKeyConstraint(baseColumnNames: "segment_id", baseTableName: "playlist_segment", constraintName: "FK_mm0f4iykod5okc3r3si6np8bt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "segment")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-87") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "client_auto_approve_scopes", constraintName: "FK_n6cxvsci8hxdq6td8lfy4fqyu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-88") {
        addForeignKeyConstraint(baseColumnNames: "creator_id", baseTableName: "video_creator", constraintName: "FK_pfx8g7bae1wqsrulrtmhp8x3n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-89") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "user_client", constraintName: "FK_qn4u3odpng1dx0x10xhrad5cp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-90") {
        addForeignKeyConstraint(baseColumnNames: "reel_id", baseTableName: "reel_video", constraintName: "FK_qt1pauuh9ywm37kl0jtkttvtb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "reel")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-91") {
        addForeignKeyConstraint(baseColumnNames: "client_id", baseTableName: "client_redirect_uris", constraintName: "FK_rder7xgc17qf5tll7cotmd6vi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "client")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-92") {
        addForeignKeyConstraint(baseColumnNames: "user_clients_id", baseTableName: "user_client", constraintName: "FK_rot2o8h9ydfcs3u4migr8rnx7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }

    changeSet(author: "bvandiver (generated)", id: "1460669746193-93") {
        addForeignKeyConstraint(baseColumnNames: "video_id", baseTableName: "user_activity", constraintName: "FK_uks9dj7lmsi7idwxjkc1vtxq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "video")
    }
}
