package com.arm.ntahahasetwitter.services;

/**
* IPC interface for methods on XMPPService called by an activity
*/

interface ITimelineService {
	void futchStatus();
	void fetchStatus(int page, int count, long sinceId, long maxId);
}