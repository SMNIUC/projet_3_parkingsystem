package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {
	
	private ParkingService parkingService;
	
	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;
	@Mock
	private static FareCalculatorService fareCalculatorService;

	private Ticket ticket;

	private ParkingSpot parkingSpot;
	
	@Test
	void testProcessIncomingVehicle() throws Exception{
		//GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
		when(ticketDAO.saveTicket(any())).thenReturn(true);
		when(ticketDAO.getNbTicket(any())).thenReturn(0);
		
		//WHEN
		ticket = parkingService.processIncomingVehicle();
		
		//THEN
		assertThat(ticket.getParkingSpot()).isNotNull();
		assertThat(ticket.getParkingSpot().getId()).isEqualTo(1);
		assertThat(ticket.getVehicleRegNumber()).isEqualTo("ABCDEF");
		assertThat(ticket.getPrice()).isZero();
	}
	
	@Test
	void testProcessExitingVehicle() throws Exception {
		//GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		when(ticketDAO.getTicket(any())).thenReturn(ticket);
		when(ticketDAO.getNbTicket(any())).thenReturn(1);
		when(ticketDAO.updateTicket(any())).thenReturn(true);
		
		//WHEN
		parkingService.processExitingVehicle();
		
		//THEN
		assertThat(ticket.getParkingSpot()).isNotNull();
		assertThat(ticket.getPrice()).isEqualTo(1.5);
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
	}
	
	@Test
	void testProcessExitingVehicleDiscount() throws Exception {
		//GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		when(ticketDAO.getTicket(any())).thenReturn(ticket);
		when(ticketDAO.getNbTicket(any())).thenReturn(2);
		when(ticketDAO.updateTicket(any())).thenReturn(true);
		
		//WHEN
		parkingService.processExitingVehicle();
		
		//THEN
		assertThat(ticket.getParkingSpot()).isNotNull();
		assertThat(ticket.getPrice()).isEqualTo(1.43);
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
	}
	
	@Test
	void processExitingVehicleTestUnableUpdate() throws Exception{
		// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		when(ticketDAO.getTicket(any())).thenReturn(ticket);
		when(ticketDAO.getNbTicket(any())).thenReturn(2);
		when(ticketDAO.updateTicket(ticket)).thenReturn(false);
		
		// WHEN
		parkingService.processExitingVehicle();
		
		// THEN
		assertThat(parkingSpot.isAvailable()).isFalse();
	}
	
	@Test
	void testGetNextParkingNumberIfAvailable() throws Exception {
		// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
		
		// WHEN
		parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		
		// THEN
		assertThat(parkingSpot.getId()).isEqualTo(1);
		assertThat(parkingSpot.isAvailable()).isTrue();
	}

	
	@Test
	void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
		// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(0);
		
		// WHEN
		//...
		
		// THEN
		assertThrows(Exception.class, () -> { parkingService.getNextParkingNumberIfAvailable(); });

	}
	
	@Test
	void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
		// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		when(inputReaderUtil.readSelection()).thenReturn(3);
		
		// WHEN
		//...
		
		// THEN
		assertThrows(IllegalArgumentException.class, () -> { parkingService.getNextParkingNumberIfAvailable(); });

	}
	
}
