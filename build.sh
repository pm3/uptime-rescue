  mvn clean package -DskipTests
  docker build -t molnar33/uptime-rescue:0.1.0 .
  docker push molnar33/uptime-rescue:0.1.0