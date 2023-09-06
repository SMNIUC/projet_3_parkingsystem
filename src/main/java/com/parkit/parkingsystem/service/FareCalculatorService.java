package com.parkit.parkingsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ) {
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        
        // Setting the inTime and outTime in minutes
        long inTimeInMinutes = ticket.getInTime().getTime() / 60 / 1000;
        long outTimeInMinutes = ticket.getOutTime().getTime() / 60 / 1000;
        
        // Calculating the duration in minutes
        long durationInMinutes = outTimeInMinutes - inTimeInMinutes;

        // Setting price to 0 if vehicle stays less than 30 minutes
        if(durationInMinutes <= 30) {
        	ticket.setPrice(0);
        }
        
        // Calculating price with discount -> discount @param = true
        else {
	        if(discount) {
	        	switch (ticket.getParkingSpot().getParkingType()) {
	            case CAR: {
	                ticket.setPrice(round((durationInMinutes) * Fare.CAR_RATE_PER_HOUR * 0.95 / 60, 2));
	                break;
	            }
	            case BIKE: {
	                ticket.setPrice(round((durationInMinutes) * Fare.BIKE_RATE_PER_HOUR * 0.95 / 60, 2));
	                break;
	            }
	            default: throw new IllegalArgumentException("Unknown Parking Type");
	        	}
	        }
	        
	        // Calculating regular price without discount -> discount @param = false
	        else {
		        switch (ticket.getParkingSpot().getParkingType()) {
		        case CAR: {
		            ticket.setPrice(round((durationInMinutes) * Fare.CAR_RATE_PER_HOUR / 60, 2));
		            break;
		        }
		        case BIKE: {
		            ticket.setPrice(round((durationInMinutes)* Fare.BIKE_RATE_PER_HOUR / 60, 2));
		            break;
		        }
		        default: throw new IllegalArgumentException("Unknown Parking Type");
		    	}
	        }
        }
    }
    
    // Method to round to two decimals
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public void calculateFare(Ticket ticket) {
    	calculateFare(ticket, false);
    }
    
}