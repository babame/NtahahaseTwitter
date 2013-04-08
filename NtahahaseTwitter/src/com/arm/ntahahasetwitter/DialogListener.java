package com.arm.ntahahasetwitter;


public interface DialogListener {

	/**
	 * Called when a dialog completes.
	 * 
	 * Executed by the thread that initiated the dialog.
	 * 
	 * @param url
	 *            Key-value string pairs extracted from the response.
	 */
	public void onComplete(String url);

	/**
	 * Called when a dialog is canceled by the user.
	 * 
	 * Executed by the thread that initiated the dialog.
	 * 
	 */
	public void onCancel();
}