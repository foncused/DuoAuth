package me.foncused.duoauth.enumerable;

public enum DatabaseOption {

	JSON("json");
	//SQLITE("sqlite");

	private final String option;

	DatabaseOption(final String option) {
		this.option = option;
	}

	@Override
	public String toString() {
		return this.option;
	}

}
