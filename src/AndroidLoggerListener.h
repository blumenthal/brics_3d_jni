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

#ifndef ANDROIDLOGGERLISTENER_H_
#define ANDROIDLOGGERLISTENER_H_

#include <brics_3d/core/Logger.h>

namespace brics_3d {

class AndroidLoggerListener : public Logger::Listener {
public:
	AndroidLoggerListener();
	virtual ~AndroidLoggerListener();

	// Implemetation of listener.
	void write(Logger::Loglevel level, std::string message);
};

} /* namespace brics_3d */

#endif /* ANDROIDLOGGERLISTENER_H_ */

/* EOF */
