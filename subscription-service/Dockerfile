FROM golang:alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN go build -o subscription-service ./main.go


FROM alpine:3.21

WORKDIR /app

COPY --from=builder /app/subscription-service .

EXPOSE 8082

CMD ["./subscription-service"]
