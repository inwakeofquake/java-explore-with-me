package ru.practicum.main_service.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main_service.enums.EventState;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.main_service.utility.Constants.DATE;

@Getter
@Setter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String annotation;
    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    private int confirmedRequests;
    @Column(name = "created_On")
    private LocalDateTime createdOn;
    private String description;

    private LocalDateTime eventDate;
    @OneToOne
    @JoinColumn(name = "initiator_id", referencedColumnName = "id")
    private User initiator;
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;
    private Boolean paid;
    private int participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @NotBlank
    private String title;
    private Long views;
    @Transient
    private final String datePattern = DATE;
    @Transient
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

    public Event(Long id, String annotation, Category category, int confirmedRequests, LocalDateTime createdOn,
                 String description, LocalDateTime eventDate, User initiator, Location location, Boolean paid,
                 Integer participantLimit, LocalDateTime publishedOn, Boolean requestModeration, EventState eventState, String title, Long views) {
        this.annotation = annotation;
        this.category = category;
        this.confirmedRequests = confirmedRequests;
        if (createdOn == null) {
            this.createdOn = LocalDateTime.now();
        } else {
            this.createdOn = createdOn;
        }

        this.description = description;
        this.eventDate = eventDate;
        this.id = id;
        this.initiator = initiator;
        this.location = location;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.publishedOn = publishedOn;
        if (requestModeration == null) {
            this.requestModeration = true;
        } else {
            this.requestModeration = requestModeration;
        }
        if (eventState == null) {
            this.state = EventState.PENDING;
        } else {
            this.state = eventState;
        }
        this.title = title;
        this.views = views;
    }
}