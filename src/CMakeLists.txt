# compile and link the executables

ADD_LIBRARY(brics_3d-jni SHARED 
    be_kuleuven_mech_rsg_jni_RsgJNI.cpp
    AndroidLoggerListener.cpp
    )
TARGET_LINK_LIBRARIES(brics_3d-jni ${Boost_LIBRARIES} ${BRICS_3D_LIBRARIES} log)

