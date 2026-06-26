package vn.vuavuive.backend.modules.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Review> findById(String id) {
        return Optional.ofNullable(firebase.get("reviews/" + id, Review.class));
    }

    public List<Review> findAll() {
        return firebase.getList("reviews", Review.class);
    }

    public Review save(Review review) {
        if (review.getId() == null) {
            review.setId(UUID.randomUUID().toString());
        }
        firebase.save("reviews/" + review.getId(), review);
        return review;
    }

    public void deleteById(String id) {
        firebase.delete("reviews/" + id);
    }

    private <T> Page<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public Page<Review> findByProductId(UUID productId, Pageable pageable) {
        if (productId == null) return new PageImpl<>(List.of(), pageable, 0);
        String prodIdStr = productId.toString();
        List<Review> sorted = findAll().stream()
                .filter(r -> prodIdStr.equals(r.getProductId()) && !Boolean.TRUE.equals(r.getIsHidden()))
                .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return paginate(sorted, pageable);
    }

    public Optional<Review> findByUserIdAndProductId(String userId, String productId) {
        if (userId == null || productId == null) return Optional.empty();
        return findAll().stream()
                .filter(r -> userId.equals(r.getUserId()) && productId.equals(r.getProductId()))
                .findFirst();
    }

    public Double getAverageRatingByProductId(UUID productId) {
        if (productId == null) return 0.0;
        String prodIdStr = productId.toString();
        List<Review> list = findAll().stream()
                .filter(r -> prodIdStr.equals(r.getProductId()) && !Boolean.TRUE.equals(r.getIsHidden()))
                .collect(Collectors.toList());
        if (list.isEmpty()) return 0.0;
        double sum = list.stream().mapToInt(Review::getRating).sum();
        return sum / list.size();
    }

    public long countByProductIdAndIsHiddenFalse(UUID productId) {
        if (productId == null) return 0;
        String prodIdStr = productId.toString();
        return findAll().stream()
                .filter(r -> prodIdStr.equals(r.getProductId()) && !Boolean.TRUE.equals(r.getIsHidden()))
                .count();
    }
}
