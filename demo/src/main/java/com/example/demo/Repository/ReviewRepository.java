package com.example.demo.Repository;
import com.example.demo.DTO.AnimeDTO;
import com.example.demo.DTO.ResultSetDTO;
import com.example.demo.Model.Review;
import com.example.demo.Repository.MongoDB.ReviewRepositoryMongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Repository
public class ReviewRepository {


    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private ReviewRepositoryMongo revMongo;

    public void addReview(Review review){
        try{
            revMongo.save(review);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Review> getReviewsByUsername(String username) {
        List<Review> revList;
        try{
            revList = revMongo.findByProfile(username);
            if(revList.isEmpty())
                return null;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return revList;
    }

    //This function check if a username has already made a review for that Anime
    public boolean getReviewsByUsernameAndAnime(String username, String anime) {
        return revMongo.existsByProfileAndAnime(username, anime);
    }



    public List<ResultSetDTO> animeMostReviewed() {

        // grouping by age.
        GroupOperation groupOperation = Aggregation.group("anime").count().as("NumberReviews");

        ProjectionOperation projectFields = project()
                .andExpression("_id").as("field1")
                .andExpression("NumberReviews").as("field2");

        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "NumberReviews"));

        Aggregation aggregation = Aggregation.newAggregation(groupOperation, sortOperation, projectFields);

        AggregationResults<ResultSetDTO> result = mongoOperations.aggregate(aggregation, "reviews", ResultSetDTO.class);

        return result.getMappedResults();
    }


    public List<ResultSetDTO> groupByAnime() {

        // grouping by age.
        GroupOperation groupOperation = Aggregation.group("anime").avg("score").as("AvgScore");

        // filtering same age count > 1

        MatchOperation matchOperation = Aggregation.match(new Criteria("AvgScore").gt(5));

        ProjectionOperation projectFields = project()
                .andExpression("_id").as("field1")
                .andExpression("AvgScore").as("field2");

        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "AvgScore"));;

        Aggregation aggregation = Aggregation.newAggregation(groupOperation, matchOperation, sortOperation, projectFields);

        AggregationResults<ResultSetDTO> result = mongoOperations.aggregate(aggregation, "reviews", ResultSetDTO.class);

        return result.getMappedResults();
    }

    public List<ResultSetDTO> GetSuggestedAnime(String how_order) {

        // grouping by age.
        GroupOperation groupOperation = Aggregation.group("title").count().as("NumberReviews").avg("score").as("AvgScore");

        // filtering same age count > 1

        MatchOperation matchOperation = Aggregation.match(new Criteria("AvgScore").gt(5));

        ProjectionOperation projectFields = project()
                .andExpression("_id").as("field1")
                .andExpression("NumberReviews").as("field2")
                .andExpression("AvgScore").as("field3");

        SortOperation sortOperation;
        if(how_order.equals("DESC")) {
            sortOperation = sort(Sort.by(Sort.Direction.DESC, "AvgScore", "NumberReviews"));
        } else {
            sortOperation = sort(Sort.by(Sort.Direction.ASC, "AvgScore", "NumberReviews"));
        }

        AggregationOperation limit = Aggregation.limit(5);

        Aggregation aggregation = Aggregation.newAggregation(groupOperation, matchOperation, sortOperation, limit, projectFields);

        AggregationResults<ResultSetDTO> result = mongoOperations.aggregate(aggregation, "reviews", ResultSetDTO.class);

        return result.getMappedResults();
    }
}