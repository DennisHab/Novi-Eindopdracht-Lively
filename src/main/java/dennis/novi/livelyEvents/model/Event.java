package dennis.novi.livelyEvents.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JsonIgnoreProperties({"events", "image","reviews"})
    @JoinColumn(name ="venue_id")
    @ManyToOne
    Venue venue;

    @OneToMany(mappedBy = "event")
    @JsonIgnoreProperties("event")
    private List<Review> reviews;

    @Column
    private String type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @JsonFormat(pattern= "dd-MM-yyyy")
    private Date date;


    @Column(nullable = false)
    @JsonFormat(pattern="HH:mm")
    private String time;

    @Column
    private double rating;

    @Column
    private boolean ticketRequired;

    @ElementCollection
    private List<String> images;

    @Column
    private String eventDescription;

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isTicketRequired() {
        return ticketRequired;
    }

    public void setTicketRequired(boolean ticketRequired) {
        this.ticketRequired = ticketRequired;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

}
