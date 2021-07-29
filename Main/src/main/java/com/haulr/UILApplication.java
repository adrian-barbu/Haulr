/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.haulr;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.haulr.parse.model.CreditCard;
import com.haulr.parse.model.Service;
import com.haulr.parse.model.ServiceInfo;
import com.haulr.parse.model.User;
import com.haulr.parse.model.UserInfo;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

/**
 * @author Adrian
 */
public class UILApplication extends Application {

	// Attention this is not android key, is server key
	public final static String GOOGLE_SERVER_API_KEY = "AIzaSyDcBuVik4BpMSOelUAHFQZFxN3_YtOoZGI";

	@Override
	public void onCreate() {
		super.onCreate();

		// Initial Objects
		ParseObject.registerSubclass(User.class);
		ParseObject.registerSubclass(UserInfo.class);
		ParseObject.registerSubclass(Service.class);
		ParseObject.registerSubclass(ServiceInfo.class);
		ParseObject.registerSubclass(CreditCard.class);

		Parse.initialize(this);
		Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
		ParseFacebookUtils.initialize(this);
		FacebookSdk.sdkInitialize(getApplicationContext());

		initImageLoader(getApplicationContext());
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config.build());
	}
}