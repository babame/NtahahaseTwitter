package com.arm.ntahahasetwitter.services;



public interface Twitterable {
	void fetchStatus(int page, int count, long sinceId, long maxId);
	void fetchStatus();
	
	void updateStatus(String status, double latitude, double longitude);
}
