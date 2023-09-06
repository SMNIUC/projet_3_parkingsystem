package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private Ticket ticketIT;
    private Ticket ticketIN;
    private ParkingSpot parkingSpotIT;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    
    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
    	dataBasePrepareService.clearDataBaseEntries();
    }

	@Test
    void testParkingACar() throws Exception {
		// GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        	// creating a parking spot to compare to
        parkingSpotIT = new ParkingSpot(1, ParkingType.CAR, false);
        
        // WHEN
        ticketIT = parkingService.processIncomingVehicle();
        
        // THEN
			//TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        assertThat(ticketIT.getVehicleRegNumber()).isEqualTo("ABCDEF");
        assertThat(ticketIT.getPrice()).isZero();
        assertThat(ticketIT.getInTime()).isNotNull();
        assertThat(ticketIT.getParkingSpot()).isEqualTo(parkingSpotIT);
	}

    @Test
    void testParkingLotExit() throws Exception {
        // GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpotIN = new ParkingSpot(1, ParkingType.CAR, false);
        	// Creating a new ticket with a in-time in the past
        ticketIN = new Ticket();
        ticketIN.setParkingSpot(parkingSpotIN); 
        ticketIN.setVehicleRegNumber("ABCDEF");
        ticketIN.setPrice(0);
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - ( 60 * 60 * 1000 ));
        ticketIN.setInTime(inTime);
        ticketIN.setOutTime(null);
        ticketDAO.saveTicket(ticketIN);
        
        // WHEN
        ticketIN = parkingService.processExitingVehicle();
        
        // THEN
        	//TODO: check that the fare generated and out time are populated correctly in the database
        assertThat(ticketIN.getOutTime()).isNotNull();
        assertThat(ticketIN.getPrice()).isEqualTo(1.5);
        assertThat(ticketIN.getOutTime()).isAfter(ticketIN.getInTime());
    } 
    
    @Test
    void testParkingLotExitRecurringUser() throws Exception {
    	// GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	
    	// WHEN
    	testParkingLotExit();
    	parkingService.processIncomingVehicle();
    	Ticket ticketOUT = parkingService.processExitingVehicle();
    	
    	// THEN
    		//TODO: check that the discount price is populated correctly in the database in the case of a recurring user
    	assertThat(ticketOUT.getPrice()).isEqualTo(1.43);
    }
}    
        
        
        
        