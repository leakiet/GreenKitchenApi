# MenuMealReview API Test Documentation

## 1. Create Menu Meal Review
**POST** `/apis/v1/customers/menu-meal-reviews`

**Request Body:**
```json
{
    "menuMealId": 6,
    "customerId": 1,
    "rating": 5,
    "comment": "Rất ngon!"
}
```

**Success Response:**
```json
{
    "id": 2,
    "menuMeal": {
        "id": 6,
        "title": "Pho bo"
    },
    "customer": {
        "id": 1,
        "firstName": "Nguyen",
        "lastName": "Van A"
    },
    "rating": 5,
    "comment": "Rất ngon!",
    "createdAt": "2025-01-27T10:30:00",
    "updatedAt": "2025-01-27T10:30:00"
}
```

**Error Response:**
```json
"Customer has already reviewed this menu meal"
```

## 2. Get Reviews by Menu Meal ID
**GET** `/apis/v1/menu-meal-reviews/menu-meal/6`

**Success Response:**
```json
[
    {
        "id": 2,
        "menuMeal": {...},
        "customer": {...},
        "rating": 5,
        "comment": "Rất ngon!",
        "createdAt": "2025-01-27T10:30:00",
        "updatedAt": "2025-01-27T10:30:00"
    }
]
```

## 3. Get Reviews by Customer ID
**GET** `/apis/v1/menu-meal-reviews/customer/1`

## 4. Update Review
**PUT** `/apis/v1/customers/menu-meal-reviews/2`

**Request Body:**
```json
{
    "menuMealId": 6,
    "customerId": 1,
    "rating": 4,
    "comment": "Updated comment"
}
```

## 5. Delete Review
**DELETE** `/apis/v1/customers/menu-meal-reviews/2`

**Success Response:**
```json
"Review deleted successfully"
```

## Common Validation Errors:
- Rating must be between 1-5
- MenuMealId and CustomerId are required
- Comment max 500 characters
