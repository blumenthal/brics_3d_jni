CMAKE_MINIMUM_REQUIRED(VERSION 2.6)
PROJECT(brics_3d-jni)

SET(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/external/cmake_modules")
FIND_PACKAGE(BRICS_3D REQUIRED)
FIND_PACKAGE(Eigen REQUIRED)
#FIND_PACKAGE(OpenSceneGraph COMPONENTS osgGA osgUtil osgViewer osgText)
ADD_DEFINITIONS(-DEIGEN3)


# search for HDF5 (>= 1.8.9)
#
# for version 1.8.12 or higher please define the following flag : HDF_1_8_12_OR_HIGHER
# due to incompatible API changes in the HDF5 library.
OPTION(USE_HDF5 "Enable HDF5 Support" OFF)
IF(USE_HDF5)

    # only newer versions of FindHDF5.cmake exctract the HDF5_ROOT from the environment variable
    # - so we do it manually here
    IF("$ENV{HDF5_ROOT}" STREQUAL "")
        #nothing special here      
    ELSE("$ENV{HDF5_ROOT}" STREQUAL "")
        SET(HDF5_ROOT $ENV{HDF5_ROOT})
        SET(USE_HDF5_NON_DEFAULT_PATH TRUE) 
        MESSAGE(STATUS "INFO: HDF5_ROOT set as: $ENV{HDF5_ROOT}")
    ENDIF("$ENV{HDF5_ROOT}" STREQUAL "")
  
    FIND_PACKAGE(HDF5 REQUIRED COMPONENTS CXX HL)
    IF(USE_HDF5_NON_DEFAULT_PATH) #override results
        SET(HDF5_CXX_INCLUDE_DIR ${HDF5_ROOT}/include)        
        SET(HDF5_LIBRARIES 
            ${HDF5_ROOT}/lib/libhdf5_hl_debug.so
            ${HDF5_ROOT}/lib/libhdf5_cpp_debug.so
            ${HDF5_ROOT}/lib/libhdf5_debug.so
            #CACHE STRING "Accumulated HDF5 libraries."   
        )
    ENDIF(USE_HDF5_NON_DEFAULT_PATH)   
    MESSAGE(STATUS "INFO: used HDF5_LIBRARIES: ${HDF5_LIBRARIES}")

    IF (HDF5_FOUND)
      MESSAGE(STATUS "SUCCESSFUL: HDF5 found")
      ADD_DEFINITIONS(-DBRICS_HDF5_ENABLE)
      INCLUDE_DIRECTORIES(${HDF5_CXX_INCLUDE_DIR})     
    ELSE (HDF5_FOUND)
      MESSAGE(SEND_ERROR "WARNING: HDF5 not found.")
    ENDIF (HDF5_FOUND)
ENDIF(USE_HDF5)

IF(ANDROID)
    #JNI is found automatically
ELSE(ANDROID)
    MESSAGE(STATUS "This is not an Android build. Searching for JNI.")
    FIND_PACKAGE(JNI)
    IF (JNI_FOUND)
        MESSAGE (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
        MESSAGE (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
        INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})      
    ELSE(JNI_FOUND)
        MESSAGE(SEND_ERROR "ERROR: JNI not found.")
    ENDIF(JNI_FOUND)
ENDIF(ANDROID)

# add include directories (-I)
INCLUDE_DIRECTORIES(${BRICS_3D_INCLUDE_DIRS})
INCLUDE_DIRECTORIES(${EIGEN_INCLUDE_DIR})

# add library directories (-L)
LINK_DIRECTORIES(${BRICS_3D_LINK_DIRECTORIES})

ADD_SUBDIRECTORY(src)