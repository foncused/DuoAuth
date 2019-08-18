package me.foncused.duoauth.enumerable;

public enum DatabaseOption {

	JSON("json");
	//SQLITE("sqlite");
	//MYSQL("mysql");

	private final String option;

	DatabaseOption(final String option) {
		this.option = option;
	}

	@Override
	public String toString() {
		return this.option;
	}

}
