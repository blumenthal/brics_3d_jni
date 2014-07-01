/******************************************************************************
 * BRICS_3D - 3D Perception and Modeling Library
 * Copyright (c) 2014, KU Leuven
 *
 * Author: Sebastian Blumenthal
 *
 *
 * This software is published under a dual-license: GNU Lesser General Public
 * License LGPL 2.1 and Modified BSD license. The dual-license implies that
 * users of this code may choose which terms they prefer.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License LGPL and the BSD license for
 * more details.
 *
 ******************************************************************************/

#include "AndroidLoggerListener.h"
#include <android/log.h>

namespace brics_3d {

/* Macros for Android loggers */
#define ANDROID_LOGGER_INFO(...) ((void)__android_log_print(ANDROID_LOG_INFO, "rsg-jni", __VA_ARGS__))
#define ANDROID_LOGGER_DEBUG(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "rsg-jni", __VA_ARGS__))
#define ANDROID_LOGGER_WARNING(...) ((void)__android_log_print(ANDROID_LOG_WARN, "rsg-jni", __VA_ARGS__))
#define ANDROID_LOGGER_ERROR(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "rsg-jni", __VA_ARGS__))

AndroidLoggerListener::AndroidLoggerListener() {


}

AndroidLoggerListener::~AndroidLoggerListener() {

}

void AndroidLoggerListener::write(Logger::Loglevel level, std::string message) {

	switch (level) {

	case Logger::FATAL:
		ANDROID_LOGGER_ERROR(message.c_str());
		break;

	case Logger::LOGERROR:
		ANDROID_LOGGER_ERROR(message.c_str());
		break;

	case Logger::WARNING:
		ANDROID_LOGGER_WARNING(message.c_str());
		break;

	case Logger::INFO:
		ANDROID_LOGGER_INFO(message.c_str());
		break;

	case Logger::LOGDEBUG:
		ANDROID_LOGGER_DEBUG(message.c_str());
		break;

	default:
		break;
	}

}

} /* namespace brics_3d */

/* EOF */
