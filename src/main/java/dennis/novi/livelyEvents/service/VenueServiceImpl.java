package dennis.novi.livelyEvents.service;

import dennis.novi.livelyEvents.exception.BadRequestException;
import dennis.novi.livelyEvents.exception.FileStorageException;
import dennis.novi.livelyEvents.exception.NotAuthorizedException;
import dennis.novi.livelyEvents.exception.RecordNotFoundException;
import dennis.novi.livelyEvents.model.*;
import dennis.novi.livelyEvents.repository.UserOwnerRepository;
import dennis.novi.livelyEvents.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VenueServiceImpl implements VenueService {
    @Value("${app.upload.dir:${C:\\Users\\DennisHabets\\WebstormProjects\\novi-eindopdracht-lively\\public}}")
    public String uploadDir;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    UserOwnerRepository userOwnerRepository;


    @Override
    public List<Venue> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        for (int i = 0; i < venues.size(); i++) {
            Venue venue = venues.get(i);
            venue.setRating(calculateAverageRating(venue));
        }
        return venues;
    }
    @Override
    public Venue getVenue(Long id) {
        if (venueRepository.existsById(id)) {
            Venue venue = venueRepository.findById(id).get();
            venue.setRating(calculateAverageRating(venue));
            return venue;
        } else {
            throw new RecordNotFoundException("This id doesn't exist: " + id);
        }
    }
    @Override
    public List<Venue> getVenueVenueNameStartsWith(String venueName){
        return venueRepository.findAllByVenueNameStartingWith(venueName);
    }
    @Override
    public void save(Venue venue) {
        venue.setRating(calculateAverageRating(venue));

        venueRepository.save(venue);
    }
    @Override
    public void deleteById(Long id) {
        venueRepository.deleteById(id);
    }
    @Override
    public Optional<Venue> getVenueByVenueName(String venueName) {
        if (venueRepository.existsVenueByVenueName(venueName)) {
            return venueRepository.findByVenueName(venueName);
        } else {
            throw new RecordNotFoundException("This venue doesn't exist");
        }
    }


    @Override
    public List<Venue> findVenueByCityName(String cityName){
        List<Venue> venues = venueRepository.findAll();
        List<Address> addressVenues = new ArrayList<>();
        List<Venue> results = new ArrayList<>();
        for (Venue venue : venues) {
            addressVenues.add(venue.getAddress());
        }
        for (Address address : addressVenues) {
            if (address.getCity().contains(cityName)) {
                results.add(address.getVenue());
            }
        }
        return results;
    }
    @Override
    public void deleteUserVenueById(String username, Long id){
        UserOwner user = userOwnerRepository.findById(username).get();
        Venue userVenue = venueRepository.findById(id).get();
        List<Venue> userVenues = user.getVenueList();
        List<Long> userVenueId = new ArrayList<>();
        for (Venue venue : userVenues) {
            userVenueId.add(venue.getId());
        }
        if (userVenueId.contains(id)){
            userVenues.remove(userVenue);
            venueRepository.deleteById(id);
        } else {throw new BadRequestException("Id doesn't belong to user");}
    }

    @Override
    public Double calculateAverageRating(Venue venue) {
        List<Double> venueReviewRatings = new ArrayList<>();
        if (venue.getReviews() == null){
            venueReviewRatings.add(6.0);
        } else {List<Review> venueReviews = venue.getReviews();
            for (Review review : venueReviews) {
                venueReviewRatings.add(review.getRating());
            }
        }
        double totalReviewRating = 0.0;
        for (Double venueReviewRating : venueReviewRatings) {
            totalReviewRating = totalReviewRating + venueReviewRating;
        }
        if (venue.getReviews() == null) {
            return 6.0; } else {
            List<Review> venueTotalReviews = venue.getReviews();
            int totalReviews = venueTotalReviews.size();
            return Math.round((totalReviewRating/totalReviews) * 10.0) / 10.0;
        }
    }
    @Override
    public Venue addVenueToUser(Venue venue, String username){
        UserOwner userOwner = userOwnerRepository.findById(username).get();
        if (venueRepository.existsVenueByVenueName(venue.getVenueName())) throw new BadRequestException("VenueName is already taken, please choose another one");
        if(userOwner.getUsername() == username && venue.getUserOwner() == null) {
            List<Venue> userOwnerVenues = userOwner.getVenueList();
            userOwnerVenues.add(venue);
            venue.setRating(calculateAverageRating(venue));
            venue.setUserOwner(userOwner);
            venueRepository.save(venue);
    }   else {
            throw new RecordNotFoundException("Either the user doesn't exist or the venue already has an owner");
        }
        return venue;
    }
    @Override
    public void uploadImageToVenue(MultipartFile file, Long id, String username) {
        UserOwner userOwner = userOwnerRepository.findById(username).get();
        List<Venue> userVenues = userOwner.getVenueList();
        List<Long> userVenueIds = new ArrayList<>();
        userVenues.forEach(venue -> userVenueIds.add(venue.getId()));
        if(userVenueIds.contains(id)){
            try {
                Path copyLocation = Paths
                        .get(uploadDir +  File.separator + StringUtils.cleanPath("venue" + id + file.getOriginalFilename()));
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
                Venue venue = venueRepository.findById(id).get();
                venue.setImage("venue" + id + file.getOriginalFilename());
                venueRepository.save(venue);

            } catch (Exception e) {
                e.printStackTrace();
                throw new FileStorageException("Could not store file " + file.getOriginalFilename()
                        + ". Please try again!");
            }} else throw new NotAuthorizedException("This venue is not yours");
    }
}
