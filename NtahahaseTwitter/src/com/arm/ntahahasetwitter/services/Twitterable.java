package com.arm.ntahahasetwitter.services;



public interface Twitterable {
	void fetchStatus(int page, int count, long sinceId, long maxId);
	void fetchStatus();
	
	void updateStatus(String status, String filepath, double latitude, double longitude);
}
