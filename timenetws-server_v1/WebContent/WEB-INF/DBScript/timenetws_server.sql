-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Aug 13, 2014 at 06:18 PM
-- Server version: 5.6.17
-- PHP Version: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `timenetws_server`
--

-- --------------------------------------------------------

--
-- Table structure for table `simulation_list`
--

CREATE TABLE IF NOT EXISTS `simulation_list` (
  `ref_id` int(11) NOT NULL AUTO_INCREMENT,
  `simulation_file_name` varchar(1000) DEFAULT NULL,
  `file_data` mediumblob,
  `distribute_flag` varchar(10) DEFAULT NULL,
  `current_time_stamp` varchar(50) DEFAULT NULL,
  `upload_sim_manager` varchar(100) DEFAULT NULL,
  `file_size` int(255) DEFAULT NULL,
  PRIMARY KEY (`ref_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=47 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
