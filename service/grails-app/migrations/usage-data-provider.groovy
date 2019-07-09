databaseChangeLog = {  
	changeSet(author: "claudia (manual)", id: "2019-07-09-00001") {
		createTable(tableName: "usage_data_provider") {
			column(name: "udp_id", type: "VARCHAR(36)") {
				constraints(nullable: "false")
			}
			column(name: "udp_version", type: "BIGINT") {
				constraints(nullable: "false")
			}
			column(name: "udp_remote_id", type: "VARCHAR(36)") {
				constraints(nullable: "false")
		    }
			column(name: "udp_owner_fk", type: "VARCHAR(36)") {
				constraints(nullable: "false")
		    }
			column(name: "udp_note", type: "CLOB")
		}
	  }
	  
	  changeSet(author: "claudia (manual)", id: "2019-07-09-00002") {
		  addPrimaryKey(columnNames: "udp_id", constraintName: "usage_data_providerPK", tableName: "usage_data_provider")
	  }
	  
	  changeSet(author: "claudia (manual)", id: "2019-07-09-00003") {
		  addForeignKeyConstraint(baseColumnNames: "udp_owner_fk",
			  					  baseTableName: "usage_data_provider", 
								  constraintName: "udp_to_sa_fk",
								  deferrable: "false", initiallyDeferred: "false",
								  referencedColumnNames: "sa_id", referencedTableName: "subscription_agreement")
	  }
}
